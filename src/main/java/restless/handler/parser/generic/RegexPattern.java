package restless.handler.parser.generic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;

public class RegexPattern extends org.codehaus.jparsec.pattern.Pattern
{
	final java.util.regex.Pattern pattern;

	private RegexPattern(final Pattern pattern)
	{
		this.pattern = checkNotNull(pattern);
	}

	public static Parser<String> parserForRegex(final String name, final String regex)
	{
		return parserFor(name, Pattern.compile(regex));
	}

	public static Parser<String> parserFor(final String name, final Pattern pattern)
	{
		return Scanners.pattern(new RegexPattern(pattern), name).source();
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
			return MISMATCH;
		}
	}

}