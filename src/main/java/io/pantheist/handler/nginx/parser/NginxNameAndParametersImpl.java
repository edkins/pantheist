package io.pantheist.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class NginxNameAndParametersImpl implements NginxNameAndParameters
{
	private final NginxNodeFactory nodeFactory;
	private final String name;
	private final String ws;
	private final List<NginxWord> parameters;

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
		this.parameters = checkNotNull(parameters);
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		sb.append(name).append(ws);
		parameters.forEach(p -> p.toStringBuilder(sb));
		return sb;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public List<String> parameters()
	{
		return Lists.transform(parameters, NginxWord::value);
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

	@Override
	public void setSingleParameter(final String value)
	{
		parameters.clear();
		parameters.add(nodeFactory.word(value, ""));
	}

}
