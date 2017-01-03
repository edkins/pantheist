package io.pantheist.api.sql.backend;

public class SqlBackendException extends RuntimeException
{
	private static final long serialVersionUID = 3528527928740807276L;

	public SqlBackendException(final Exception e)
	{
		super(e);
	}

}
