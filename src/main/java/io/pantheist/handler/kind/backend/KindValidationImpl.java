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

import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.java.model.JavaModelFactory;
import io.pantheist.handler.kind.model.Entity;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindModelFactory;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.handler.sql.model.SqlProperty;

final class KindValidationImpl implements KindValidation
{
	private static final String PARENT_KIND = "parentKind";
	private static final String QUALIFIED_NAME = "qualifiedName";
	private static final String JAVA_FILE = "java-file";
	private final KindStore kindStore;
	private final KindModelFactory modelFactory;
	private final SqlService sqlService;
	private final ObjectMapper objectMapper;
	private final JavaModelFactory javaFactory;

	@Inject
	private KindValidationImpl(
			final KindStore kindStore,
			final KindModelFactory modelFactory,
			final SqlService sqlService,
			final ObjectMapper objectMapper,
			final JavaModelFactory javaFactory)
	{
		this.kindStore = checkNotNull(kindStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.sqlService = checkNotNull(sqlService);
		this.objectMapper = checkNotNull(objectMapper);
		this.javaFactory = checkNotNull(javaFactory);
	}

	@Override
	public Entity discoverJavaKind(final JavaFileId javaFileId)
	{
		return differentiate(javaEntity(javaFileId));
	}

	@Override
	public AntiIterator<Entity> listAllEntitiesWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return discoverEntitiesWithKindRecursive(kindId, new HashSet<>())
				.map(this::differentiate); // may be a subkind of the one specified
	}

	/**
	 * Entity already has a kind specified, but we want to go further and discover
	 * if it matches any child kind.
	 *
	 * This is recursive, so if it matches one it will go on and try to differentiate it
	 * further.
	 */
	private Entity differentiate(final Entity entity)
	{
		checkNotNull(entity);
		OtherPreconditions.checkNotNullOrEmpty(entity.kindId());
		final List<Kind> kinds = kindStore.listChildKinds(entity.kindId()).toList();

		for (final Kind kind : kinds)
		{
			final Optional<Entity> newEntity = differentiationStep(entity, kind);
			if (newEntity.isPresent())
			{
				return differentiate(newEntity.get());
			}
		}

		return entity;
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
	 * This is the counterpart to {{@link #differentiate(Entity)}. This looks "up" the chain,
	 * while differentiate looks "down". It's assumed the chain will end with one of the builtin kinds,
	 * which we have special logic to handle.
	 */
	private AntiIterator<Entity> discoverEntitiesWithKindRecursive(final String kindId,
			final Set<String> alreadyVisited)
	{
		final Optional<Kind> optKind = kindStore.getKind(kindId);

		if (!optKind.isPresent())
		{
			// Kind does not exist
			return AntiIt.empty();
		}

		final Kind kind = optKind.get();
		if (kind.partOfSystem())
		{
			return discoverEntitiesWithBuiltinKind(kind);
		}

		if (kind.schema().identification() == null || !kind.schema().identification().has(PARENT_KIND))
		{
			// Not a valid user-defined kind: no parentKind specified
			return AntiIt.empty();
		}

		final String parentId = kind.schema().identification().get(PARENT_KIND).textValue();

		if (alreadyVisited.contains(parentId))
		{
			// You horrible person
			return AntiIt.empty();
		}
		alreadyVisited.add(parentId);

		return discoverEntitiesWithKindRecursive(parentId, alreadyVisited)
				.optMap(entity -> differentiationStep(entity, kind));
	}

	/**
	 * Attempt to differentiate the given entity into the given kind, which must be a child kind.
	 *
	 * Returns empty if the attempt failed.
	 */
	private Optional<Entity> differentiationStep(final Entity entity, final Kind kind)
	{
		if (!kind.hasParent(entity.kindId()))
		{
			throw new IllegalArgumentException("Should already have checked that kind is a child of entity's kind "
					+ entity.kindId() + "," + kind.kindId());
		}
		if (!entity.canDifferentiate())
		{
			return Optional.empty();
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
					if (!entity.propertyValues().has(name))
					{
						// A mistake? Entity should have all the relevant property values specified.
						return Optional.empty();
					}

					final JsonNode value = entity.propertyValues().get(name);

					if (!matchValueAgainstSchema(value, entry.getValue()))
					{
						return Optional.empty();
					}
				}
			}
		}

		// Otherwise assume ok.
		return Optional.of(modelFactory.entity(
				entity.entityId(),
				kind.kindId(),
				entity.jsonSchemaId(),
				entity.javaFileId(),
				entity.propertyValues(),
				true));
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

	private AntiIterator<Entity> discoverEntitiesWithBuiltinKind(final Kind kind)
	{
		if (!kind.isBuiltinKind())
		{
			throw new IllegalStateException("Not a valid builtin kind");
		}

		switch (kind.kindId()) {
		case JAVA_FILE:
			final List<SqlProperty> propertyList = kindStore.listSqlPropertiesOfKind(JAVA_FILE).toList();
			return sqlService.select(JAVA_FILE, propertyList)
					.execute()
					.map(this::jsonNodeToEntity);
		default:
			// Not listing other things for now.
			return AntiIt.empty();
		}
	}

	private Entity jsonNodeToEntity(final ObjectNode obj)
	{
		return modelFactory.entity(
				obj.get(QUALIFIED_NAME).textValue(),
				JAVA_FILE,
				null,
				javaFactory.fileId(obj.get("package").textValue(), obj.get("fileName").textValue()),
				obj,
				true);
	}

	@Deprecated
	private Entity javaEntity(final JavaFileId javaFileId)
	{
		checkNotNull(javaFileId);

		// Impose an arbitrary order on properties
		final List<SqlProperty> propertyList = kindStore.listSqlPropertiesOfKind(JAVA_FILE).toList();

		final JsonNode index = objectMapper.getNodeFactory().textNode(javaFileId.qualifiedName());

		final Optional<ObjectNode> propertyValues = sqlService.select(JAVA_FILE, propertyList)
				.whereEqual(QUALIFIED_NAME, index)
				.execute()
				.failIfMultiple();

		if (propertyValues.isPresent())
		{
			return modelFactory.entity(javaFileId.file(), JAVA_FILE, null,
					javaFileId, propertyValues.get(), true);
		}
		else
		{
			// was not found in sql, so assume invalid. Set canDifferentiate to
			// false to avoid confusing the subkinds.
			return modelFactory.entity(javaFileId.file(), JAVA_FILE, null,
					javaFileId, objectMapper.getNodeFactory().objectNode(), false);
		}

	}
}
