package restless.handler.filesystem.except;

public class FsConflictException extends RuntimeException
{
	private static final long serialVersionUID = -671886728473272882L;

	public FsConflictException(final String message)
	{
		super(message);
	}
}
