package io.pantheist.handler.filekind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.filesystem.backend.FileState;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.filesystem.backend.FsPathMapping;
import io.pantheist.handler.filesystem.except.FsParseException;
import io.pantheist.handler.filesystem.except.FsUnexpectedStateException;
import io.pantheist.plugin.interfaces.AlteredSnapshot;

final class AlteredSnapshotImpl implements WriteableAlteredSnapshot
{
	private final FilesystemSnapshot snapshot;
	private final Consumer<AlteredSnapshot> changeHook;
	private final ObjectMapper objectMapper;
	private final FsPath baseDir;

	// State
	private final List<Alteration> alterations;
	private boolean writing;
	private boolean forceChangeNotification;

	private AlteredSnapshotImpl(
			final FilesystemSnapshot snapshot,
			final Consumer<AlteredSnapshot> changeHook,
			final ObjectMapper objectMapper,
			final FsPath baseDir)
	{
		this.snapshot = checkNotNull(snapshot);
		this.changeHook = checkNotNull(changeHook);
		this.objectMapper = checkNotNull(objectMapper);
		this.baseDir = baseDir;
		this.alterations = new ArrayList<>();
		this.writing = false;
		this.forceChangeNotification = false;
	}

	static WriteableAlteredSnapshot of(
			final FilesystemSnapshot snapshot,
			final Consumer<AlteredSnapshot> changeHook,
			final ObjectMapper objectMapper,
			final FsPath baseDir)
	{
		return new AlteredSnapshotImpl(snapshot, changeHook, objectMapper, baseDir);
	}

	@Override
	public void writeOutEverything()
	{
		if (this.writing)
		{
			throw new IllegalStateException("AlteredSnapshot already written");
		}
		this.writing = true;
		if (!alterations.isEmpty() || forceChangeNotification)
		{
			changeHook.accept(this);
		}
		snapshot.write(this::writeAlterations);
	}

	private void writeAlterations(final FsPathMapping map) throws IOException
	{
		for (final Alteration alt : alterations)
		{
			final File file = map.get(alt.path());
			switch (alt.type()) {
			case CREATE_FILE:
			case UPDATE_FILE:
				FileUtils.writeStringToFile(file, alt.text(), StandardCharsets.UTF_8);
				break;
			case DELETE_FILE:
				file.delete();
				break;
			default:
				throw new UnsupportedOperationException("Unrecognized alteration: " + alt.type());
			}
		}
	}

	private Optional<Alteration> latestAlteration(final FsPath path)
	{
		for (int i = alterations.size() - 1; i >= 0; i--)
		{
			if (alterations.get(i).path().equals(path))
			{
				return Optional.of(alterations.get(i));
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean isFile(final FsPath path)
	{
		final FileState state = checkFileState(path);
		switch (state) {
		case REGULAR_FILE:
			return true;
		case DOES_NOT_EXIST:
			return false;
		default:
			throw new FsUnexpectedStateException("Exists but not a regular file: " + path + " in state " + state);
		}
	}

	@Override
	public boolean safeIsFile(final FsPath path)
	{
		return checkFileState(path) == FileState.REGULAR_FILE;
	}

	@Override
	public FileState checkFileState(final FsPath path)
	{
		final Optional<Alteration> alt = latestAlteration(path);
		if (alt.isPresent())
		{
			return alt.get().fileState();
		}
		return snapshot.checkFileState(path);
	}

	@Override
	public void deleteFile(final FsPath path)
	{
		final FileState state = checkFileState(path);
		if (state == FileState.REGULAR_FILE)
		{
			alterations.add(Alteration.of(AlterationType.DELETE_FILE, path, null));
		}
		else
		{
			throw new FsUnexpectedStateException("Delete - not a regular file: " + path + " in state " + state);
		}
	}

	@Override
	public void writeText(final FsPath path, final String text)
	{
		final FileState state = checkFileState(path);
		switch (state) {
		case REGULAR_FILE:
			alterations.add(Alteration.of(AlterationType.UPDATE_FILE, path, text));
			break;
		case DOES_NOT_EXIST:
			alterations.add(Alteration.of(AlterationType.CREATE_FILE, path, text));
			break;
		default:
			throw new FsUnexpectedStateException(
					"Write - exists but not a regular file: " + path + " in state " + state);
		}
	}

	@Override
	public <T> void writeJson(final FsPath path, final T data)
	{
		try
		{
			writeText(path, objectMapper.writeValueAsString(data));
		}
		catch (final JsonProcessingException e)
		{
			throw new FsParseException(e);
		}
	}

	@Override
	public String readText(final FsPath path)
	{
		final Optional<Alteration> alt = latestAlteration(path);
		if (alt.isPresent())
		{
			final FileState state = alt.get().fileState();
			if (state == FileState.REGULAR_FILE)
			{
				return alt.get().text();
			}
			else
			{
				throw new FsUnexpectedStateException("Read - not a regular file: " + path + " in state " + state);
			}
		}
		else
		{
			final FileState state = checkFileState(path);
			if (state == FileState.REGULAR_FILE)
			{
				return snapshot.readText(path);
			}
			else
			{
				throw new FsUnexpectedStateException("Read - not a regular file: " + path + " in state " + state);
			}
		}
	}

	@Override
	public AntiIterator<FsPath> listFilesAndDirectories(final FsPath dir)
	{
		final Set<FsPath> set = new HashSet<>();
		snapshot.listFilesAndDirectories(dir).forEach(set::add);
		for (final Alteration alt : alterations)
		{
			if (alt.path().isChildOf(dir))
			{
				switch (alt.type()) {
				case CREATE_FILE:
				case UPDATE_FILE:
					set.add(alt.path());
					break;
				case DELETE_FILE:
					set.remove(alt.path());
					break;
				default:
					throw new UnsupportedOperationException("Unrecognized alteration: " + alt.type());
				}
			}
		}
		return AntiIt.from(set);
	}

	@Override
	public FsPath baseDir()
	{
		return baseDir;
	}

	@Override
	public <T> T readJson(final FsPath path, final Class<T> clazz)
	{
		try
		{
			return objectMapper.readValue(readText(path), clazz);
		}
		catch (final IOException e)
		{
			throw new FsParseException(e);
		}
	}

	@Override
	public void forceChangeNotification()
	{
		this.forceChangeNotification = true;
	}

}
