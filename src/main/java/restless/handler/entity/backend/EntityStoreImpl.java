package restless.handler.entity.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.inject.Inject;

import restless.handler.entity.model.Entity;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.JsonSnapshot;

final class EntityStoreImpl implements EntityStore
{
	private final FilesystemStore store;

	@Inject
	private EntityStoreImpl(final FilesystemStore store)
	{
		this.store = checkNotNull(store);
	}

	@Override
	public void putEntity(final String entityId, final Entity entity)
	{
		if (entity.discovered())
		{
			throw new IllegalArgumentException("Entity store is not intended to be used for discovered entities");
		}
		final JsonSnapshot<Entity> snapshot = store.jsonSnapshot(path(entityId), Entity.class);

		snapshot.exists(); // don't care
		snapshot.write(entity);
	}

	private FsPath path(final String entityId)
	{
		return store.systemBucket().segment("entity").segment(entityId);
	}

	@Override
	public Optional<Entity> getEntity(final String entityId)
	{
		final JsonSnapshot<Entity> snapshot = store.jsonSnapshot(path(entityId), Entity.class);

		if (snapshot.exists())
		{
			return Optional.of(snapshot.read());
		}
		else
		{
			return Optional.empty();
		}
	}

}
