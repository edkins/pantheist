package restless.handler.nginx.parser;

import java.util.Optional;

final class StringHelpers
{
	private StringHelpers()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * @param str a string that maybe ends in whitespace
	 * @return
	 */
	public static Optional<String> indentationAtEnd(final String str)
	{
		int i = str.length() - 1;
		boolean foundNewLine = false;
		while (i >= 0)
		{
			final char ch = str.charAt(i);
			if (ch == ' ' || ch == '\t')
			{
				// carry on
			}
			else if (ch == '\n')
			{
				foundNewLine = true;
				break;
			}
			else
			{
				break;
			}
			i--;
		}
		if (foundNewLine)
		{
			return Optional.of(str.substring(i + 1));
		}
		else
		{
			return Optional.empty();
		}
	}

	public static String nlIndent(final String str)
	{
		final Optional<String> indent = indentationAtEnd(str);
		if (indent.isPresent())
		{
			return "\n" + indent.get();
		}
		else
		{
			return "\n";
		}
	}
}
