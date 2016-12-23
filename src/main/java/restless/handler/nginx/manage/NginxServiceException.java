package restless.handler.nginx.manage;

public class NginxServiceException extends RuntimeException
{
	private static final long serialVersionUID = 2501818173048000459L;

	public NginxServiceException(final Exception e)
	{
		super(e);
	}

	public NginxServiceException(final String message)
	{
		super(message);
	}

}
