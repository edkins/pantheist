package io.pantheist.api.sql.backend;

import java.sql.SQLException;

public class SqlBackendException extends RuntimeException
{
	private static final long serialVersionUID = 3528527928740807276L;

	public SqlBackendException(final SQLException e)
	{
		super(e);
	}

}
