package restless.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

final class NginxRootImpl implements NginxRoot
{
	private final String ws;
	private final NginxCollection contents;

	@Inject
	private NginxRootImpl(
			final NginxNodeFactory nodeFactory,
			@Assisted("ws") final String ws,
			@Assisted final List<NginxDirective> contents)
	{
		checkNotNull(contents);
		this.contents = nodeFactory.collection(new ArrayList<>(contents), StringHelpers.nlIndent(ws));
		this.ws = checkNotNull(ws);
	}

	@Override
	public NginxCollection contents()
	{
		return contents;
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

	private StringBuilder toStringBuilder(final StringBuilder sb)
	{
		sb.append(ws);
		contents.list().forEach(i -> i.toStringBuilder(sb));
		return sb;
	}
}
