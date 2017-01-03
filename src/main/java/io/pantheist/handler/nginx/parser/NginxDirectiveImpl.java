package io.pantheist.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

final class NginxDirectiveImpl implements NginxDirective
{
	private final NginxNameAndParameters nameAndParameters;
	private final NginxBlock block;

	@Inject
	private NginxDirectiveImpl(
			@Assisted final NginxNameAndParameters nameAndParameters,
			@Assisted final NginxBlock block)
	{
		this.nameAndParameters = checkNotNull(nameAndParameters);
		this.block = checkNotNull(block);
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		nameAndParameters.toStringBuilder(sb);
		return block.toStringBuilder(sb);
	}

	@Override
	public NginxBlock contents()
	{
		return block;
	}

	@Override
	public String name()
	{
		return nameAndParameters.name();
	}

	@Override
	public List<String> parameters()
	{
		return nameAndParameters.parameters();
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

	@Override
	public void setSingleParameter(final String value)
	{
		nameAndParameters.setSingleParameter(value);
	}

	@Override
	public String nlIndent()
	{
		return block.nlIndent();
	}

	@Override
	public void padTo(final String nlIndent)
	{
		block.padTo(nlIndent);
	}
}
