package io.pantheist.handler.parser.generic;

public class SyntaxParserException extends RuntimeException
{
	private static final long serialVersionUID = 4213130780178758510L;

	public SyntaxParserException(final Exception ex)
	{
		super(ex);
	}
}
