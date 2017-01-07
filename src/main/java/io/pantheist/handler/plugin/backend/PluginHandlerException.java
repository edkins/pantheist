package io.pantheist.handler.plugin.backend;

public class PluginHandlerException extends RuntimeException
{
	private static final long serialVersionUID = 7578659448229795200L;

	public PluginHandlerException(final String message)
	{
		super(message);
	}

	public PluginHandlerException(final Exception e)
	{
		super(e);
	}
}
