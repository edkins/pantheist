package io.pantheist.handler.parser.generic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class SyntaxTreeNodeTerminalImpl implements SyntaxTreeNode
{
	private final SyntaxSymbol symbol;

	// Mutable
	private String text;

	@Inject
	private SyntaxTreeNodeTerminalImpl(@Assisted final SyntaxSymbol symbol, @Assisted("text") final String text)
	{
		if (symbol.type() != SyntaxSymbolType.TERMINAL)
		{
			throw new IllegalArgumentException("Terminal node cannot be constructed for non-terminal symbol " + symbol);
		}
		this.symbol = checkNotNull(symbol);
		this.text = checkNotNull(text);
	}

	@Override
	public SyntaxSymbol symbol()
	{
		return symbol;
	}

	@Override
	public void append(final SyntaxTreeNode node)
	{
		throw new UnsupportedOperationException("Terminal node for " + symbol + " does not support append()");
	}

	@Override
	public void remove(final int index)
	{
		throw new UnsupportedOperationException("Terminal node for " + symbol + " does not support remove()");
	}

	@Override
	public String toString()
	{
		return text;
	}

	@Override
	public ImmutableList<SyntaxTreeNode> children()
	{
		return ImmutableList.of();
	}

	@Override
	public StringBuilder debugToString(final StringBuilder sb)
	{
		return sb.append(symbol.name()).append('"').append(text).append('"');
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		return sb.append(text);
	}

	@Override
	public void modify(final String text)
	{
		symbol.parse(text);
		this.text = text;
	}

	@Override
	public void changeChildren(final List<SyntaxTreeNode> newChildren)
	{
		throw new UnsupportedOperationException("Terminal node for " + symbol + " does not support changeChildren()");
	}
}
