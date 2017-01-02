package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.java.backend.JavaStore;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Entity;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindModelFactory;

final class KindValidationImpl implements KindValidation
{
	private final JavaStore javaStore;
	private final KindStore kindStore;
	private final KindModelFactory modelFactory;

	@Inject
	private KindValidationImpl(
			final JavaStore javaStore,
			final KindStore kindStore,
			final KindModelFactory modelFactory)
	{
		this.javaStore = checkNotNull(javaStore);
		this.kindStore = checkNotNull(kindStore);
		this.modelFactory = checkNotNull(modelFactory);
	}

	/**
	 * Validate without regards for superkinds.
	 */
	private boolean validateIndividual(final Entity entity, final Kind kind)
	{
		if (kind.schema().java() != null)
		{
			if (!javaStore.validateKind(entity.javaFileId(), kind.schema().java()))
			{
				// Java store says the details are invalid.
				return false;
			}
		}

		// Otherwise assume ok.
		return true;
	}

	@Override
	public Entity discoverJavaKind(final JavaFileId javaFileId)
	{
		checkNotNull(javaFileId);
		final Entity entity = modelFactory.entity(javaFileId.file(), "java-file", null,
				javaFileId);

		return differentiate(entity);
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
			if (validateIndividual(entity, kind))
			{
				return differentiate(supplyKind(entity, kind));
			}
		}

		return entity;
	}

	private Entity supplyKind(final Entity entity, final Kind kind)
	{
		return modelFactory.entity(
				entity.entityId(),
				kind.kindId(),
				entity.jsonSchemaId(),
				entity.javaFileId());
	}

	@Override
	public AntiIterator<Entity> discoverEntitiesWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return discoverEntitiesWithKindRecursive(kindId, new HashSet<>())
				.map(this::differentiate); // may be a subkind of the one specified
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

		if (kind.schema().identification() == null || !kind.schema().identification().has("parentKind"))
		{
			// Not a valid user-defined kind: no parentKind specified
			return AntiIt.empty();
		}

		final String parentId = kind.schema().identification().get("parentKind").textValue();

		if (alreadyVisited.contains(parentId))
		{
			// You horrible person
			return AntiIt.empty();
		}
		alreadyVisited.add(parentId);

		return discoverEntitiesWithKind(parentId)
				.filter(entity -> validateIndividual(entity, kind))
				.map(e -> supplyKind(e, kind));
	}

	private AntiIterator<Entity> discoverEntitiesWithBuiltinKind(final Kind kind)
	{
		if (!kind.partOfSystem()
				|| (kind.schema().identification() != null && kind.schema().identification().has("parentKind")))
		{
			throw new IllegalStateException("Not a valid builtin kind");
		}

		switch (kind.kindId()) {
		case "java-file":
			return javaStore.allJavaFiles()
					.map(jf -> modelFactory.entity(jf.file(), kind.kindId(), null, jf));
		default:
			// Not listing other things for now.
			return AntiIt.empty();
		}
	}
}
