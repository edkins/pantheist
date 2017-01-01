package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.filesystem.backend.JsonSnapshot;
import io.pantheist.handler.kind.model.Kind;

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
	public AntiIterator<Kind> listAllKinds()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.listFilesAndDirectories(kindDir())
				.filter(snapshot::safeIsFile)
				.map(path -> snapshot.readJson(path, Kind.class));
	}
}
