package io.pantheist.handler.parser.generic;

import org.codehaus.jparsec.pattern.Pattern;

public class EmptyPatternImpl extends Pattern
{

	@Override
	public int match(final CharSequence src, final int begin, final int end)
	{
		return 0;
	}

}
