package io.pantheist.handler.nginx.parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class NginxBlockEmptyImpl implements NginxBlock
{
	private String delim;

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
	public NginxDirective getOrCreateSimple(final String name)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public NginxDirective getOrCreateBlock(final String name)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public NginxDirective addBlock(final String name, final List<String> parameters)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public List<NginxDirective> getAll(final String name)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public boolean deleteAllByName(final String name)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public Optional<String> lookup(final String name)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public boolean removeIf(final Predicate<NginxDirective> predicate)
	{
		throw new UnsupportedOperationException("simple directive does not contain other elements");
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public void padTo(final String nlIndent)
	{
		delim = StringHelpers.padTo(delim, nlIndent);
	}
}
