package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.shared.model.GenericPropertyValue;
import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.java.backend.JavaStore;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Entity;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindModelFactory;
import io.pantheist.handler.kind.model.KindProperty;
import io.pantheist.handler.sql.backend.SqlService;

final class KindValidationImpl implements KindValidation
{
	private static final String PARENT_KIND = "parentKind";
	private static final String QUALIFIED_NAME = "qualifiedName";
	private static final String JAVA_FILE = "java-file";
	private final JavaStore javaStore;
	private final KindStore kindStore;
	private final KindModelFactory modelFactory;
	private final SqlService sqlService;
	private final CommonSharedModelFactory sharedFactory;

	@Inject
	private KindValidationImpl(
			final JavaStore javaStore,
			final KindStore kindStore,
			final KindModelFactory modelFactory,
			final SqlService sqlService,
			final CommonSharedModelFactory sharedFactory)
	{
		this.javaStore = checkNotNull(javaStore);
		this.kindStore = checkNotNull(kindStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.sqlService = checkNotNull(sqlService);
		this.sharedFactory = checkNotNull(sharedFactory);
	}

	@Override
	public Entity discoverJavaKind(final JavaFileId javaFileId)
	{
		return differentiate(javaEntity(javaFileId));
	}

	@Override
	public AntiIterator<Entity> discoverEntitiesWithKind(final String kindId)
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
					if (!entity.propertyValues().containsKey(name))
					{
						// A mistake? Entity should have all the relevant property values specified.
						return Optional.empty();
					}

					final GenericPropertyValue value = entity.propertyValues().get(name);

					// In the future there will be non-exact matches.
					// But these need to be inherited from ancestor kinds, so it would be more complicated.
					if (!value.matchesJsonNodeExactly(entry.getValue()))
					{
						return Optional.empty();
					}
				}
			}
		}

		if (kind.schema().java() != null)
		{
			if (!javaStore.validateKind(entity.javaFileId(), kind.schema().java()))
			{
				// Java store says the details are invalid.
				return Optional.empty();
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

	private AntiIterator<Entity> discoverEntitiesWithBuiltinKind(final Kind kind)
	{
		if (!kind.isBuiltinKind())
		{
			throw new IllegalStateException("Not a valid builtin kind");
		}

		switch (kind.kindId()) {
		case JAVA_FILE:
			return javaStore.allJavaFiles().map(this::javaEntity);
		default:
			// Not listing other things for now.
			return AntiIt.empty();
		}
	}

	private Entity javaEntity(final JavaFileId javaFileId)
	{
		checkNotNull(javaFileId);

		final Kind javaKind = kindStore.getKind(JAVA_FILE).get();

		// Impose an arbitrary order on properties
		final List<Entry<String, KindProperty>> propertyList = ImmutableList
				.copyOf(javaKind.schema().properties().entrySet());
		final List<String> columnNames = Lists.transform(propertyList, Entry::getKey);

		final GenericPropertyValue index = sharedFactory.stringValue(QUALIFIED_NAME, javaFileId.qualifiedName());

		final Optional<Map<String, GenericPropertyValue>> propertyValues = sqlService
				.selectIndividualRow(JAVA_FILE, index, columnNames)
				.map(rs -> rsCollectProperties(rs, propertyList))
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
					javaFileId, ImmutableMap.of(), false);
		}

	}

	private Map<String, GenericPropertyValue> rsCollectProperties(final ResultSet rs,
			final List<Entry<String, KindProperty>> propertyList)
	{
		try
		{
			final ImmutableMap.Builder<String, GenericPropertyValue> builder = ImmutableMap.builder();

			for (int i = 0; i < propertyList.size(); i++)
			{
				final String name = propertyList.get(i).getKey();
				final PropertyType type = propertyList.get(i).getValue().type();

				switch (type) {
				case STRING:
					builder.put(name, sharedFactory.stringValue(name, rs.getString(i + 1)));
					break;
				case BOOLEAN:
					builder.put(name, sharedFactory.booleanValue(name, rs.getBoolean(i + 1)));
					break;
				}
			}
			return builder.build();
		}
		catch (final SQLException e)
		{
			throw new KindValidationException(e);
		}
	}
}
