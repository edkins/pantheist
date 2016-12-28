package restless.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

final class NginxBlockImpl implements NginxBlock
{
	private final NginxCollection contents;
	private final String delim1;
	private final String delim2;

	@Inject
	private NginxBlockImpl(
			final NginxNodeFactory nodeFactory,
			@Assisted("delim1") final String delim1,
			@Assisted final List<NginxDirective> contents,
			@Assisted("delim2") final String delim2)
	{
		this.delim1 = checkNotNull(delim1);
		this.contents = nodeFactory.collection(new ArrayList<>(contents), StringHelpers.nlIndent(delim1));
		this.delim2 = checkNotNull(delim2);
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		sb.append(delim1);
		contents.list().basic().list().forEach(d -> d.toStringBuilder(sb));
		return sb.append(delim2);
	}

	@Override
	public NginxCollection contents()
	{
		return contents;
	}
}
