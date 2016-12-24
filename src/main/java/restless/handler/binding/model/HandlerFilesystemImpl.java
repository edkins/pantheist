package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.handler.filesystem.backend.FsPath;

final class HandlerFilesystemImpl implements Handler
{
	private final FsPath bucket;

	@Inject
	HandlerFilesystemImpl(@Assisted @JsonProperty("bucket") final FsPath bucket)
	{
		checkNotNull(bucket);
		if (bucket.isEmpty())
		{
			throw new IllegalArgumentException("Cannot bind to filesystem root");
		}
		this.bucket = bucket;
	}

	@Override
	public HandlerType type()
	{
		return HandlerType.filesystem;
	}

	@JsonProperty("bucket")
	private FsPath bucket()
	{
		return bucket;
	}

	@Override
	public FsPath filesystemBucket()
	{
		return bucket;
	}
}
