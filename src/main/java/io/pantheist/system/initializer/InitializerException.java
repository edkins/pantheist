package io.pantheist.system.initializer;

public class InitializerException extends RuntimeException
{
	private static final long serialVersionUID = 5074897127583301935L;

	public InitializerException(final Exception e)
	{
		super(e);
	}

	public InitializerException(final String message)
	{
		super(message);
	}
}
