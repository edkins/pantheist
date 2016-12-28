package restless.handler.nginx.parser;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class NginxBlockEmptyImpl implements NginxBlock
{
	private final String delim;

	@Inject
	private NginxBlockEmptyImpl(@Assisted("delim") final String delim)
	{
		this.delim = OtherPreconditions.checkNotNullOrEmpty(delim);
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		return sb.append(delim);
	}

	@Override
	public NginxCollection contents()
	{
		throw new UnsupportedOperationException("Empty node has no contents");
	}
}
