package io.pantheist.handler.filesystem.except;

public class FsParseException extends RuntimeException
{
	private static final long serialVersionUID = -8001932137521029782L;

	public FsParseException(final Exception e)
	{
		super(e);
	}
}
