package io.pantheist.testhelpers.rule;

public class ServerNeverAppearedException extends Exception
{
	private static final long serialVersionUID = -7949223961548617794L;

	public ServerNeverAppearedException(final Exception e)
	{
		super(e);
	}
}
