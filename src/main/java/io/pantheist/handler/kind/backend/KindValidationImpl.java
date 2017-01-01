package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.entity.model.Entity;
import io.pantheist.handler.entity.model.EntityModelFactory;
import io.pantheist.handler.java.backend.JavaStore;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Kind;

final class KindValidationImpl implements KindValidation
{
	private final JavaStore javaStore;
	private final KindStore kindStore;
	private final EntityModelFactory entityFactory;

	@Inject
	private KindValidationImpl(
			final JavaStore javaStore,
			final KindStore kindStore,
			final EntityModelFactory entityFactory)
	{
		this.javaStore = checkNotNull(javaStore);
		this.kindStore = checkNotNull(kindStore);
		this.entityFactory = checkNotNull(entityFactory);
	}

	/**
	 * Return a list of all superkinds (including the kind itself). Each will be listed at most once.
	 *
	 * If there are no cycles then they will be listed with the most super first.
	 * i.e.
	 *
	 * java-file
	 * java-interface-file
	 * java-interface-with-some-crazy-annotation
	 *
	 * If there are cycles then all of the relevant kinds will be returned but the
	 * order might not be what you're expecting.
	 *
	 * kindId's that point to a missing kind won't be added.
	 */
	private AntiIterator<Kind> allSuperKinds(final Kind kind)
	{
		checkNotNull(kind);
		return consumer -> {
			recurseSuperKinds(kind, new HashSet<>(), consumer);
		};
	}

	private void recurseSuperKinds(final Kind kind, final Set<String> encountered, final Consumer<Kind> consumer)
	{
		encountered.add(kind.kindId());
		for (final String parentId : kind.subKindOf())
		{
			if (!encountered.contains(parentId))
			{
				final Possible<Kind> parentKind = kindStore.getKind(parentId);
				if (parentKind.isPresent())
				{
					recurseSuperKinds(parentKind.get(), encountered, consumer);
				}
			}
		}
		consumer.accept(kind);
	}

	@Override
	public boolean validateEntityAgainstKind(final Entity entity, final Kind kind)
	{
		return allSuperKinds(kind)
				.allMatch(k -> validateIndividual(entity, k));
	}

	/**
	 * Validate without regards for superkinds.
	 */
	private boolean validateIndividual(final Entity entity, final Kind kind)
	{
		if (!validateBuiltin(entity, kind))
		{
			return false;
		}

		if (kind.java() != null)
		{
			if (!javaStore.validateKind(entity.javaFileId(), kind.java()))
			{
				// Java store says the details are invalid.
				return false;
			}
		}

		// Otherwise assume ok.
		return true;
	}

	/**
	 * Deal with any built-in kinds with special meanings.
	 */
	private boolean validateBuiltin(final Entity entity, final Kind kind)
	{
		if (kind.partOfSystem())
		{
			if (kind.kindId().equals("java-file") && entity.javaFileId() == null)
			{
				return false;
			}
		}
		return true;
	}

	private static enum KindStatus
	{
		FAILED,
		SUPERSEDED,
		OK;
	}

	@Override
	public Entity discoverJavaKind(final JavaFileId javaFileId)
	{
		checkNotNull(javaFileId);
		final Entity entity = entityFactory.entity(javaFileId.file(), true, null, null,
				javaFileId);

		return discoverKind(entity);
	}

	private Entity discoverKind(final Entity entity)
	{
		final Map<String, KindStatus> map = new HashMap<>();
		final Map<String, Kind> kinds = kindStore.discoverKinds().toMap(Kind::kindId);

		boolean anythingHappened;
		do
		{
			anythingHappened = false;
			for (final Kind kind : kinds.values())
			{
				if (!map.containsKey(kind.kindId()))
				{
					boolean canVisit = true;
					boolean failed = false;

					// Fail if any parent failed.
					// Otherwise, not ready yet if any parent hasn't been visited.

					for (final String superId : kind.subKindOf())
					{
						if (!map.containsKey(superId))
						{
							canVisit = false;
						}
						else if (map.get(superId) == KindStatus.FAILED)
						{
							failed = true;
						}
					}

					// If we're ready, validate this kind
					if (canVisit && !failed)
					{
						failed = !validateIndividual(entity, kind);
					}

					if (failed)
					{
						map.put(kind.kindId(), KindStatus.FAILED);
						anythingHappened = true;
					}
					else if (canVisit)
					{
						for (final String superId : kind.subKindOf())
						{
							map.put(superId, KindStatus.SUPERSEDED);
						}
						map.put(kind.kindId(), KindStatus.OK);
						anythingHappened = true;
					}
				}
			}
		} while (anythingHappened);

		return supplyKind(entity, map.entrySet()
				.stream()
				.filter(e -> e.getValue().equals(KindStatus.OK))
				.map(e -> kinds.get(e.getKey()))
				.findAny()
				.orElseThrow(() -> new IllegalStateException(
						"Java file did not match any kind. java-file must be missing")));
	}

	private Entity supplyKind(final Entity entity, final Kind kind)
	{
		if (entity.kindId() != null)
		{
			throw new IllegalArgumentException("entity should not have kind yet");
		}
		return entityFactory.entity(
				entity.entityId(),
				entity.discovered(),
				kind.kindId(),
				entity.jsonSchemaId(),
				entity.javaFileId());
	}

	@Override
	public boolean validateEntityAgainstStoredKind(final Entity entity)
	{
		if (entity.kindId() == null)
		{
			// If kind is not specified then anything is valid.
			return true;
		}
		final Possible<Kind> kind = kindStore.getKind(entity.kindId());
		if (!kind.isPresent())
		{
			// If kind refers to something we don't know about, default to not being valid.
			return false;
		}
		return validateEntityAgainstKind(entity, kind.get());
	}

	@Override
	public AntiIterator<Entity> discoverEntitiesWithKind(final Kind kind)
	{
		if (!kind.discoverable())
		{
			throw new IllegalArgumentException("Can only be called for discoverable kinds");
		}
		return javaStore.allJavaFiles()
				.map(jf -> entityFactory.entity(jf.file(), true, null, null, jf))
				.filter(entity -> validateEntityAgainstKind(entity, kind))
				.map(this::discoverKind); // actual kind may be a subkind of the one requested
	}
}
