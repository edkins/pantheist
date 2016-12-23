package restless.handler.filesystem.except;

import java.io.IOException;

public class FsIoException extends RuntimeException
{
	private static final long serialVersionUID = -3258444507835013533L;

	public FsIoException(final IOException ex)
	{
		super(ex);
	}

	public FsIoException(final String message)
	{
		super(message);
	}
}
