package restless.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.inject.Inject;

import restless.common.util.AntiIterator;
import restless.handler.entity.model.Entity;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaFileId;
import restless.handler.kind.model.Kind;

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

	@Override
	public Optional<Entity> discoverJavaKind(final JavaFileId javaFileId)
	{
		final Entity entity = entityFactory.entity(javaFileId.file(), true, null, null,
				javaFileId);
		return kindStore.discoverKinds()
				.filter(k -> validateEntityAgainstKind(entity, k))
				.failIfMultiple()
				.map(k -> supplyKind(entity, k));
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
			// If kind is missing then anything is valid.
			return true;
		}
		final Kind kind = kindStore.getKind(entity.kindId()).get();
		return validateEntityAgainstKind(entity, kind);
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
