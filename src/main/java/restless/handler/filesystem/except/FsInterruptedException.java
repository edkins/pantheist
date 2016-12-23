package restless.handler.filesystem.except;

public class FsInterruptedException extends RuntimeException
{
	private static final long serialVersionUID = 6404435346130495904L;

	public FsInterruptedException(final InterruptedException ex)
	{
		super(ex);
	}
}
