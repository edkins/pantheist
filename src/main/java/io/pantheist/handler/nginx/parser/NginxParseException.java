package io.pantheist.handler.nginx.parser;

public class NginxParseException extends RuntimeException
{
	private static final long serialVersionUID = 128163625935262613L;

	public NginxParseException(final Exception e)
	{
		super(e);
	}
}
