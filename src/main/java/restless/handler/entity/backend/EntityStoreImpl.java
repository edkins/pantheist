package restless.handler.entity.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.inject.Inject;

import restless.common.util.AntiIterator;
import restless.common.util.OtherPreconditions;
import restless.handler.entity.model.Entity;
import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.JsonSnapshot;

final class EntityStoreImpl implements EntityStore
{
	private final FilesystemStore filesystem;

	@Inject
	private EntityStoreImpl(final FilesystemStore store)
	{
		this.filesystem = checkNotNull(store);
	}

	@Override
	public void putEntity(final String entityId, final Entity entity)
	{
		OtherPreconditions.checkNotNullOrEmpty(entityId);
		if (entity.discovered())
		{
			throw new IllegalArgumentException("Entity store is not intended to be used for discovered entities");
		}
		if (!entityId.equals(entity.entityId()))
		{
			throw new IllegalArgumentException("Correct entity id must be included in entity");
		}
		final JsonSnapshot<Entity> snapshot = filesystem.jsonSnapshot(path(entityId), Entity.class);

		snapshot.exists(); // don't care
		snapshot.write(entity);
	}

	private FsPath path(final String entityId)
	{
		return entityDir().segment(entityId);
	}

	private FsPath entityDir()
	{
		return filesystem.systemBucket().segment("entity");
	}

	@Override
	public Optional<Entity> getEntity(final String entityId)
	{
		final JsonSnapshot<Entity> snapshot = filesystem.jsonSnapshot(path(entityId), Entity.class);

		if (snapshot.exists())
		{
			return Optional.of(snapshot.read());
		}
		else
		{
			return Optional.empty();
		}
	}

	@Override
	public AntiIterator<Entity> listEntities()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot.listFilesAndDirectories(entityDir())
				.map(path -> snapshot.readJson(path, Entity.class));
	}

}
