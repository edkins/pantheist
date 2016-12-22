package restless.system.server;

/**
 * Thrown when the server fails to start up. Intended to be fatal.
 */
public class StartupException extends RuntimeException
{
	private static final long serialVersionUID = 8569052116319860913L;

	public StartupException(final Exception ex)
	{
		super(ex);
	}
}
