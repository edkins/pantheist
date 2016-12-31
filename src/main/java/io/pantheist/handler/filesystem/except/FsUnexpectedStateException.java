package io.pantheist.handler.filesystem.except;

public class FsUnexpectedStateException extends RuntimeException
{
	private static final long serialVersionUID = 6425797441889564581L;

	public FsUnexpectedStateException(final String message)
	{
		super(message);
	}
}
