package restless.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.common.util.AntiIterator;
import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.JsonSnapshot;
import restless.handler.kind.model.Kind;

final class KindStoreImpl implements KindStore
{
	private final FilesystemStore filesystem;

	@Inject
	private KindStoreImpl(final FilesystemStore filesystem)
	{
		this.filesystem = checkNotNull(filesystem);
	}

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind)
	{
		final JsonSnapshot<Kind> snapshot = filesystem.jsonSnapshot(path(kindId), Kind.class);

		snapshot.exists(); // don't care
		snapshot.write(kind);
		return View.noContent();
	}

	private FsPath path(final String kindId)
	{
		return kindDir().segment(kindId);
	}

	private FsPath kindDir()
	{
		return filesystem.systemBucket().segment("kind");
	}

	@Override
	public Possible<Kind> getKind(final String kindId)
	{
		final JsonSnapshot<Kind> snapshot = filesystem.jsonSnapshot(path(kindId), Kind.class);

		if (snapshot.exists())
		{
			return View.ok(snapshot.read());
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public AntiIterator<Kind> discoverKinds()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.listFilesAndDirectories(kindDir())
				.map(path -> snapshot.readJson(path, Kind.class))
				.filter(kind -> kind.discoverable());
	}

	private String pathToKindId(final FsPath path)
	{
		return path.segmentsRelativeTo(kindDir()).failIfMultiple().get();
	}

	@Override
	public AntiIterator<String> listKindIds()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.listFilesAndDirectories(kindDir())
				.filter(snapshot::safeIsFile)
				.map(this::pathToKindId);
	}

}
