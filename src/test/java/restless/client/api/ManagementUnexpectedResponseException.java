package restless.client.api;

import java.io.IOException;

/**
 * Indicates an unexpected response from the server, e.g.
 *
 * - 500 Internal Server Error
 */
public class ManagementUnexpectedResponseException extends RuntimeException
{
	private static final long serialVersionUID = -8455836673177065887L;

	public ManagementUnexpectedResponseException(final String message)
	{
		super(message);
	}

	public ManagementUnexpectedResponseException(final IOException e)
	{
		super(e);
	}
}
