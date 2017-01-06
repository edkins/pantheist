package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.pantheist.common.util.EmptyFilterableObjectStream;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.handler.sql.model.SqlProperty;

final class KindValidationImpl implements KindValidation
{
	private static final String PARENT_KIND = "parentKind";
	private static final String QUALIFIED_NAME = "qualifiedName";
	private static final String JAVA_FILE = "java-file";
	private final KindStore kindStore;
	private final SqlService sqlService;
	private final ObjectMapper objectMapper;
	private final FileKindHandler fileKindHandler;

	@Inject
	private KindValidationImpl(
			final KindStore kindStore,
			final SqlService sqlService,
			final ObjectMapper objectMapper,
			final FileKindHandler fileKindHandler)
	{
		this.kindStore = checkNotNull(kindStore);
		this.sqlService = checkNotNull(sqlService);
		this.objectMapper = checkNotNull(objectMapper);
		this.fileKindHandler = checkNotNull(fileKindHandler);
	}

	@Override
	public FilterableObjectStream objectsWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return discoverEntitiesWithKindRecursive(kindId, new HashSet<>())
				.setField("kindId", x -> differentiate(x, kindId)); // may be a subkind of the one specified
	}

	/**
	 * Entity already has a kind specified, but we want to go further and discover
	 * if it matches any child kind.
	 *
	 * This is recursive, so if it matches one it will go on and try to differentiate it
	 * further.
	 */
	private JsonNode differentiate(final ObjectNode entity, final String kindId)
	{
		checkNotNull(entity);
		final List<Kind> kinds = kindStore.listChildKinds(kindId).toList();

		for (final Kind kind : kinds)
		{
			if (differentiationStep(entity, kindId, kind))
			{
				return differentiate(entity, kind.kindId());
			}
		}

		return objectMapper.getNodeFactory().textNode(kindId);
	}

	/**
	 * Finds entities that have the specified kind or one of its subkinds.
	 *
	 * They are returned tagged with the kind specified, not the most precise refinement of it.
	 *
	 * alreadyVisited is a mutable set of kindIds that have already been visited in this call.
	 * The only purpose of this is to check for cycles, which otherwise would lead to
	 * infinite recursion.
	 *
	 * This is the counterpart to {{@link #differentiate(ObjectNode,String)}. This looks "up" the chain,
	 * while differentiate looks "down". It's assumed the chain will end with one of the builtin kinds,
	 * which we have special logic to handle.
	 */
	private FilterableObjectStream discoverEntitiesWithKindRecursive(final String kindId,
			final Set<String> alreadyVisited)
	{
		final Optional<Kind> optKind = kindStore.getKind(kindId);

		if (!optKind.isPresent())
		{
			// Kind does not exist
			return EmptyFilterableObjectStream.empty();
		}

		final Kind kind = optKind.get();
		if (kind.hasParent("file"))
		{
			return discoverFileEntities(kind);
		}
		if (kind.partOfSystem())
		{
			return discoverEntitiesWithBuiltinKind(kind);
		}

		if (kind.schema().identification() == null || !kind.schema().identification().has(PARENT_KIND))
		{
			// Not a valid user-defined kind: no parentKind specified
			return EmptyFilterableObjectStream.empty();
		}

		final String parentId = kind.schema().identification().get(PARENT_KIND).textValue();

		if (alreadyVisited.contains(parentId))
		{
			// Cycle of parent kinds. Return empty to avoid infinite recursion
			return EmptyFilterableObjectStream.empty();
		}
		alreadyVisited.add(parentId);

		return discoverEntitiesWithKindRecursive(parentId, alreadyVisited)
				.postFilter(entity -> differentiationStep(entity, parentId, kind));
	}

	/**
	 * Return whether the entity can be differentiated from kindId into the specified kind
	 * (which must be a child kind).
	 */
	private boolean differentiationStep(final ObjectNode entity, final String kindId, final Kind kind)
	{
		if (!kind.hasParent(kindId))
		{
			throw new IllegalArgumentException("Should already have checked that kind is a child of entity's kind "
					+ kindId + "," + kind.kindId());
		}
		if (entity.has("canDifferentiate") && !entity.get("canDifferentiate").booleanValue())
		{
			return false;
		}

		if (kind.schema().identification() != null)
		{
			final Iterator<Entry<String, JsonNode>> iterator = kind.schema().identification().fields();
			while (iterator.hasNext())
			{
				final Entry<String, JsonNode> entry = iterator.next();
				final String name = entry.getKey();

				if (name.equals(PARENT_KIND))
				{
					// This one is handled specially. Don't need to handle it here.
				}
				else if (entry.getValue() == null || entry.getValue().isNull())
				{
					// ignore null specifications
				}
				else
				{
					if (!entity.has(name))
					{
						// A mistake? Entity should have all the relevant property values specified.
						return false;
					}

					final JsonNode value = entity.get(name);

					if (!matchValueAgainstSchema(value, entry.getValue()))
					{
						return false;
					}
				}
			}
		}

		// Otherwise assume ok.
		return true;
	}

	private boolean matchValueAgainstSchema(final JsonNode jsonNode, final JsonNode schema)
	{
		checkNotNull(jsonNode);
		checkNotNull(schema);
		if (schema.isBoolean() || schema.isNumber() || schema.isTextual())
		{
			// These match only themselves.
			return jsonNode.equals(schema);
		}
		else if (schema.isNull())
		{
			// Null schema matches anything
			return true;
		}
		else if (schema.isObject())
		{
			final Iterator<Entry<String, JsonNode>> iterator = schema.fields();
			while (iterator.hasNext())
			{
				final Entry<String, JsonNode> entry = iterator.next();

				switch (entry.getKey()) {
				case "includes":
					if (!validateIncludes(jsonNode, entry.getValue()))
					{
						return false;
					}
					break;
				case "properties":
					if (!validateProperties(jsonNode, entry.getValue()))
					{
						return false;
					}
					break;
				default:
					throw new IllegalArgumentException("Not sure how to handle schema attribute " + entry.getKey());
				}
			}
			return true;
		}
		else
		{
			// If schema is an array or then it's invalid.
			return false;
		}
	}

	/**
	 * Check that the json value is an object that has all the fields specified here,
	 * and each value matches the corresponding schema.
	 */
	private boolean validateProperties(final JsonNode jsonNode, final JsonNode fieldSchemas)
	{
		if (!fieldSchemas.isObject())
		{
			// This really needs to be an object, otherwise the schema itself is invalid.
			return false;
		}
		final Iterator<Entry<String, JsonNode>> iterator = fieldSchemas.fields();
		while (iterator.hasNext())
		{
			final Entry<String, JsonNode> entry = iterator.next();
			final String fieldName = entry.getKey();
			final JsonNode fieldSchema = entry.getValue();
			if (!jsonNode.has(fieldName))
			{
				return false;
			}
			if (!matchValueAgainstSchema(jsonNode.get(fieldName), fieldSchema))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Check that the json value is an array which has at least one entry
	 * matching the given schema.
	 */
	private boolean validateIncludes(final JsonNode jsonNode, final JsonNode schema)
	{
		if (!jsonNode.isArray())
		{
			return false;
		}

		for (int i = 0; i < jsonNode.size(); i++)
		{
			if (matchValueAgainstSchema(jsonNode.get(i), schema))
			{
				return true;
			}
		}
		return false;
	}

	private FilterableObjectStream discoverEntitiesWithBuiltinKind(final Kind kind)
	{
		if (!kind.isBuiltinKind())
		{
			throw new IllegalStateException("Not a valid builtin kind");
		}

		switch (kind.kindId()) {
		case JAVA_FILE:
		{
			final TextNode javaFile = objectMapper.getNodeFactory().textNode(JAVA_FILE);
			final List<SqlProperty> propertyList = kindStore.listSqlPropertiesOfKind(JAVA_FILE).toList();
			return sqlService.select(JAVA_FILE, propertyList)
					.setField("kindId", x -> javaFile)
					.setField("entityId", x -> x.get(QUALIFIED_NAME));
		}
		default:
			// Not listing other things for now.
			return EmptyFilterableObjectStream.empty();
		}
	}

	private FilterableObjectStream discoverFileEntities(final Kind kind)
	{
		return fileKindHandler.discoverFileEntities(kind);
	}
}
