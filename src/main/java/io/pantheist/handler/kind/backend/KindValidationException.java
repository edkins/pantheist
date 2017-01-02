package io.pantheist.handler.kind.backend;

import java.sql.SQLException;

public class KindValidationException extends RuntimeException
{
	private static final long serialVersionUID = 5920276908578032338L;

	public KindValidationException(final SQLException e)
	{
		super(e);
	}
}
