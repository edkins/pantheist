package io.pantheist.handler.filekind.backend;

public class FileKindException extends RuntimeException
{
	private static final long serialVersionUID = 3378820910324389906L;

	public FileKindException(final String message)
	{
		super(message);
	}

	public FileKindException(final Exception e)
	{
		super(e);
	}

}
