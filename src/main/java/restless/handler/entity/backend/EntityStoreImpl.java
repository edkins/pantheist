package restless.handler.entity.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
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
	public Possible<Void> putEntity(final String entityId, final Entity entity)
	{
		final JsonSnapshot<Entity> snapshot = store.jsonSnapshot(path(entityId), Entity.class);

		snapshot.exists(); // don't care
		snapshot.write(entity);
		return View.noContent();
	}

	private FsPath path(final String entityId)
	{
		return store.systemBucket().segment("entity").segment(entityId);
	}

	@Override
	public Possible<Entity> getEntity(final String entityId)
	{
		final JsonSnapshot<Entity> snapshot = store.jsonSnapshot(path(entityId), Entity.class);

		if (snapshot.exists())
		{
			return View.ok(snapshot.read());
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

}
