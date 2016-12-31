package io.pantheist.handler.parser.generic;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class SyntaxTreeNodeListImpl implements SyntaxTreeNode
{
	private final SyntaxSymbol symbol;
	// Mutable
	private ImmutableList<SyntaxTreeNode> children;

	@Inject
	public SyntaxTreeNodeListImpl(@Assisted final SyntaxSymbol symbol, @Assisted final List<SyntaxTreeNode> children)
	{
		this.symbol = symbol;
		this.children = ImmutableList.copyOf(children);

		checkChildren(symbol, children);
	}

	private static void checkChildren(final SyntaxSymbol symbol, final List<SyntaxTreeNode> children)
	{
		switch (symbol.type()) {
		case MANY:
			for (final SyntaxTreeNode node : children)
			{
				if (!symbol.childSymbols().get(0).equals(node.symbol()))
				{
					throw new UnsupportedOperationException("Child node has incorrect symbol. Should be "
							+ symbol.childSymbols().get(0) + ", actually " + node.symbol());
				}
			}
			break;
		case SEQUENCE:
			if (children.size() != symbol.childSymbols().size())
			{
				throw new IllegalArgumentException("Attempting to create node with wrong number of children. Should be "
						+ symbol.childSymbols().size() + ", actually " + children.size());
			}
			for (int i = 0; i < children.size(); i++)
			{
				if (!children.get(i).symbol().equals(symbol.childSymbols().get(i)))
				{
					throw new IllegalArgumentException("Child number " + i + " wrong symbol. Should be "
							+ symbol.childSymbols().get(i).name() + ", actually " + children.get(i).symbol().name());
				}
			}
			break;
		case CHOICE:
		{
			if (children.size() != 1)
			{
				throw new IllegalArgumentException("Choice node must be created with 1 child. Actually got "
						+ children.size());
			}
			boolean ok = false;
			for (final SyntaxSymbol childSymbol : symbol.childSymbols())
			{
				if (childSymbol.equals(children.get(0).symbol()))
				{
					ok = true;
					break;
				}
			}
			if (!ok)
			{
				throw new IllegalArgumentException(
						"Symbol " + children.get(0).symbol() + " not ok as a choice for " + symbol);
			}
			break;
		}
		default:
			throw new UnsupportedOperationException("Cannot create a list node for symbol " + symbol);
		}
	}

	@Override
	public SyntaxSymbol symbol()
	{
		return symbol;
	}

	@Override
	public void append(final SyntaxTreeNode node)
	{
		if (symbol.type() != SyntaxSymbolType.MANY)
		{
			throw new UnsupportedOperationException("Node for " + symbol + " does not support append()");
		}
		changeChildren(ImmutableList.<SyntaxTreeNode>builder()
				.addAll(children)
				.add(node)
				.build());
	}

	@Override
	public void remove(final int index)
	{
		if (symbol.type() != SyntaxSymbolType.MANY)
		{
			throw new UnsupportedOperationException("Node for " + symbol + " does not support remove()");
		}
		if (index < 0 || index >= children.size())
		{
			throw new IndexOutOfBoundsException(
					"Index " + index + " is out of bounds for remove(). We have " + children.size() + " children.");
		}
		changeChildren(ImmutableList.<SyntaxTreeNode>builder()
				.addAll(children.subList(0, index))
				.addAll(children.subList(index + 1, children.size()))
				.build());
	}

	@Override
	public void changeChildren(final List<SyntaxTreeNode> newChildren)
	{
		checkChildren(symbol, newChildren);
		this.children = ImmutableList.copyOf(newChildren);
	}

	@Override
	public ImmutableList<SyntaxTreeNode> children()
	{
		return children;
	}

	@Override
	public StringBuilder debugToString(final StringBuilder sb)
	{
		sb.append(symbol.name());
		sb.append('{');
		boolean first = true;
		for (final SyntaxTreeNode node : children)
		{
			if (!first)
			{
				sb.append(' ');
			}
			first = false;
			node.debugToString(sb);
		}
		return sb.append('}');
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		for (final SyntaxTreeNode node : children)
		{
			node.toStringBuilder(sb);
		}
		return sb;
	}

	@Override
	public void modify(final String text)
	{
		final SyntaxTreeNode result = symbol.parse(text);
		changeChildren(result.children());
	}
}
