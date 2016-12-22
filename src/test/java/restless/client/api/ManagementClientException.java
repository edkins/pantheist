package restless.client.api;

/**
 * Thrown if something goes wrong on the client side.
 *
 * These are only for errors that occur internal to the client. 400-series
 * errors from the server will generate different kinds of exception.
 *
 * Generally these are unexpected.
 */
public class ManagementClientException extends RuntimeException
{
	private static final long serialVersionUID = 1373829688285466194L;

	public ManagementClientException(final String message, final Exception cause)
	{
		super(message);
	}
}
