package restless.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import restless.common.util.MutableListView;
import restless.common.util.OtherPreconditions;
import restless.common.util.View;

final class NginxNameAndParametersImpl implements NginxNameAndParameters
{
	private final NginxNodeFactory nodeFactory;
	private final String name;
	private final String ws;
	final MutableListView<NginxWord> parameters;

	@Inject
	private NginxNameAndParametersImpl(
			final NginxNodeFactory nodeFactory,
			@Assisted("name") final String name,
			@Assisted("ws") final String ws,
			@Assisted final List<NginxWord> parameters)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.ws = checkNotNull(ws);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.parameters = View.mutableCopy(parameters);
	}

	private NginxWord withOneSpace(final String word)
	{
		return nodeFactory.word(word, " ");
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		sb.append(name).append(ws);
		parameters.basic().list().forEach(p -> p.toStringBuilder(sb));
		return sb;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public MutableListView<String> parameters()
	{
		return parameters.translate(NginxWord::value, this::withOneSpace);
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

}
