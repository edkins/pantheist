package io.pantheist.handler.filekind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import io.pantheist.handler.filesystem.backend.FileState;
import io.pantheist.handler.filesystem.backend.FsPath;

final class Alteration
{
	private final AlterationType type;
	private final FsPath path;
	private final String text;

	private Alteration(final AlterationType type, final FsPath path, final String text)
	{
		this.type = checkNotNull(type);
		if (type == AlterationType.CREATE_FILE || type == AlterationType.UPDATE_FILE)
		{
			checkNotNull(text);
		}
		this.path = checkNotNull(path);
		this.text = text;
	}

	static Alteration of(final AlterationType type, final FsPath path, final String text)
	{
		return new Alteration(type, path, text);
	}

	public AlterationType type()
	{
		return type;
	}

	public String text()
	{
		return text;
	}

	public FsPath path()
	{
		return path;
	}

	public FileState fileState()
	{
		switch (this.type) {
		case CREATE_FILE:
		case UPDATE_FILE:
			return FileState.REGULAR_FILE;
		case DELETE_FILE:
			return FileState.DOES_NOT_EXIST;
		default:
			throw new IllegalStateException();
		}
	}
}
