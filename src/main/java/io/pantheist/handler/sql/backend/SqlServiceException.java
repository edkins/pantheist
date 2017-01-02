package io.pantheist.handler.sql.backend;

public class SqlServiceException extends RuntimeException
{
	private static final long serialVersionUID = -7035585751463210284L;

	public SqlServiceException(final String message)
	{
		super(message);
	}

	public SqlServiceException(final Exception e)
	{
		super(e);
	}
}
