package restless.api.management.backend;

public class UrlPatternMismatchException extends RuntimeException
{
	private static final long serialVersionUID = 5964171550545988361L;

	public UrlPatternMismatchException(final Exception e)
	{
		super(e);
	}

	public UrlPatternMismatchException(final String message)
	{
		super(message);
	}
}
