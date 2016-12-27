package restless.handler.parser;

import java.util.regex.Matcher;

import org.codehaus.jparsec.pattern.Pattern;

final class RegexPatternImpl extends Pattern
{
	final java.util.regex.Pattern pattern;

	RegexPatternImpl(final String regex)
	{
		pattern = java.util.regex.Pattern.compile(regex);
	}

	@Override
	public int match(final CharSequence src, final int begin, final int end)
	{
		final Matcher matcher = pattern.matcher(src.subSequence(begin, end));
		if (matcher.lookingAt())
		{
			return matcher.end();
		}
		else
		{
			return Pattern.MISMATCH;
		}
	}

}