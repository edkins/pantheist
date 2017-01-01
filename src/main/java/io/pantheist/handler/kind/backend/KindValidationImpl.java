package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.entity.model.Entity;
import io.pantheist.handler.entity.model.EntityModelFactory;
import io.pantheist.handler.java.backend.JavaStore;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindLevel;
import io.pantheist.handler.kind.model.KindModelFactory;

final class KindValidationImpl implements KindValidation
{
	private final JavaStore javaStore;
	private final KindStore kindStore;
	private final EntityModelFactory entityFactory;
	private final KindModelFactory kindModelFactory;

	@Inject
	private KindValidationImpl(
			final JavaStore javaStore,
			final KindStore kindStore,
			final EntityModelFactory entityFactory,
			final KindModelFactory kindModelFactory)
	{
		this.javaStore = checkNotNull(javaStore);
		this.kindStore = checkNotNull(kindStore);
		this.entityFactory = checkNotNull(entityFactory);
		this.kindModelFactory = checkNotNull(kindModelFactory);
	}

	@Override
	public boolean validateEntityAgainstKind(final Entity entity, final Kind kind)
	{
		if (kind.java() != null)
		{
			if (kind.java().required() && (entity.javaFileId() == null))
			{
				// Java is required but not present in entity.
				return false;
			}
			if (!javaStore.validateKind(entity.javaFileId(), kind.java()))
			{
				// Java store says the details are invalid.
				return false;
			}
		}

		// Otherwise assume ok.
		return true;
	}

	private Kind baseJavaKind()
	{
		// Priority is -1, which is lower than 0.
		// This is so that user-defined kinds which didn't specify a precedence will beat it.
		return kindModelFactory.kind("java-file", KindLevel.entity, true, null, true, -1);
	}

	@Override
	public Entity discoverJavaKind(final JavaFileId javaFileId)
	{
		final Entity entity = entityFactory.entity(javaFileId.file(), true, null, null,
				javaFileId);
		return supplyKind(entity, kindStore.discoverKinds()
				.filter(k -> validateEntityAgainstKind(entity, k))
				.max(k -> (long) k.precedence())
				.orElseGet(this::baseJavaKind));
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
				.filter(entity -> validateEntityAgainstKind(entity, kind));
	}
}
