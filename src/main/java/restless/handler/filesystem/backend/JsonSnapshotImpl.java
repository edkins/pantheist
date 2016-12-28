package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.common.util.MutableOpt;
import restless.common.util.View;
import restless.handler.filesystem.except.FsParseException;
import restless.handler.filesystem.except.FsUnexpectedStateException;

final class JsonSnapshotImpl<T> implements JsonSnapshot<T>
{
	private final FilesystemSnapshot snapshot;
	private final FsPath path;
	private final ObjectMapper objectMapper;
	private final Class<T> clazz;

	// State
	MutableOpt<T> value;

	JsonSnapshotImpl(final FilesystemSnapshot snapshot,
			final ObjectMapper objectMapper,
			final FsPath path,
			final Class<T> clazz)
	{
		this.snapshot = checkNotNull(snapshot);
		this.path = checkNotNull(path);
		this.objectMapper = checkNotNull(objectMapper);
		this.clazz = checkNotNull(clazz);
		this.value = View.mutableOpt();
	}

	@Override
	public boolean exists()
	{
		return snapshot.isFile(path);
	}

	@Override
	public T read()
	{
		if (!value.isPresent())
		{
			if (exists())
			{
				final T result = snapshot.read(path, in -> {
					try
					{
						return objectMapper.readValue(in, clazz);
					}
					catch (JsonParseException | JsonMappingException e)
					{
						throw new FsParseException(e);
					}
				});
				value.supply(result);
			}
			else
			{
				throw new FsUnexpectedStateException("Json file does not exists: " + path);
			}
		}
		return value.get();
	}

	@Override
	public void write(final T value)
	{
		try
		{
			final String data = objectMapper.writeValueAsString(value);
			snapshot.writeSingleText(path, data);
		}
		catch (final JsonProcessingException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void writeMutable()
	{
		if (!value.isPresent())
		{
			throw new IllegalStateException("Attempted writeMutable() without having called read()");
		}
		write(value.get());
	}

}
