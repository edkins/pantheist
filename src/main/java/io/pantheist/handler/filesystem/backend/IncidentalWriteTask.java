package io.pantheist.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used to keep track of incidental files that need to be written
 * or created.
 *
 * Only one of these is permitted per file.
 */
class IncidentalWriteTask
{
	private final FsPath path;
	private final SingleFileProcessor processor;

	IncidentalWriteTask(final FsPath path, final SingleFileProcessor processor)
	{
		this.path = checkNotNull(path);
		this.processor = checkNotNull(processor);
	}

	FsPath path()
	{
		return path;
	}

	SingleFileProcessor processor()
	{
		return processor;
	}
}
