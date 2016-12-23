package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.handler.filesystem.except.FsIoException;

final class LockedJsonFileImpl<T> implements LockedTypedFile<T>
{
	private final ObjectMapper objectMapper;
	private final LockedFile file;
	private final Class<T> clazz;

	LockedJsonFileImpl(final ObjectMapper objectMapper, final LockedFile file, final Class<T> clazz)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.file = checkNotNull(file);
		this.clazz = checkNotNull(clazz);
	}

	public static <T> LockedTypedFile<T> from(final ObjectMapper objectMapper, final LockedFile file,
			final Class<T> clazz)
	{
		return new LockedJsonFileImpl<>(objectMapper, file, clazz);
	}

	@Override
	public boolean fileExits()
	{
		return file.fileExists();
	}

	@Override
	public void close()
	{
		file.close();
	}

	@Override
	public T read()
	{
		try (InputStream in = file.inputStream())
		{
			return objectMapper.readValue(in, clazz);
		}
		catch (final IOException e)
		{
			throw new FsIoException(e);
		}
	}

	@Override
	public void write(final T value)
	{
		try (OutputStream out = file.outputStream())
		{
			objectMapper.writeValue(out, value);
		}
		catch (final IOException e)
		{
			throw new FsIoException(e);
		}
	}
}
