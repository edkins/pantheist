package io.pantheist.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

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

	public static String padTo(String str, final String nlIndent)
	{
		checkNotNull(str);
		checkNotNull(nlIndent);
		if (str.endsWith(nlIndent))
		{
			return str;
		}
		else
		{
			final Optional<String> whitespace = indentationAtEnd(str);
			if (whitespace.isPresent())
			{
				if (!str.endsWith("\n" + whitespace.get()))
				{
					// Something went wrong with the whitespace. Best leave it alone.
					return str;
				}
				str = str.substring(0, str.length() - whitespace.get().length() - 1);
			}
			return str + nlIndent;
		}
	}
}
