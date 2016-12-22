package restless.client.api;

public class ManagementResourceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 8789516420906093474L;

	public ManagementResourceNotFoundException(final String message)
	{
		super(message);
	}
}
