package restless.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class NginxWordImpl implements NginxWord
{
	private String word;
	private final String ws;

	static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9!\"$%&'()*+,-./:<=>?@^_`|~]+"); // must not include ; { } #

	@Inject
	private NginxWordImpl(
			@Assisted("word") final String word,
			@Assisted("ws") final String ws)
	{
		this.ws = checkNotNull(ws);
		this.word = OtherPreconditions.checkNotNullOrEmpty(word);
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		return sb.append(word).append(ws);
	}

	@Override
	public String value()
	{
		return word;
	}

	@Override
	public void setValue(final String newValue)
	{
		OtherPreconditions.checkNotNullOrEmpty(newValue);
		if (!PATTERN.matcher(newValue).matches())
		{
			throw new IllegalArgumentException("NginxWordImpl: not a valid word " + newValue);
		}
		this.word = newValue;
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

}
