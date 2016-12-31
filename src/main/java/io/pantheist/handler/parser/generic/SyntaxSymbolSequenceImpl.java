package io.pantheist.handler.parser.generic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.error.ParserException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;

final class SyntaxSymbolSequenceImpl implements SyntaxSymbolJparsec
{
	private final SyntaxTreeNodeFactory nodeFactory;
	private final String name;
	private final List<SyntaxSymbol> childSymbols;
	// Cached info
	private final Parser<SyntaxTreeNode> parser;

	@Inject
	public SyntaxSymbolSequenceImpl(
			final SyntaxTreeNodeFactory nodeFactory,
			final @Assisted("name") String name,
			final @Assisted List<SyntaxSymbol> childSymbols)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.name = checkNotNull(name);
		checkNotNull(childSymbols);
		this.childSymbols = ImmutableList.copyOf(childSymbols);

		final Parser<List<SyntaxTreeNode>> parsers = listOfParsers(name,
				Lists.transform(childSymbols, c -> ((SyntaxSymbolJparsec) c).parser()));
		this.parser = parsers.map(children -> nodeFactory.list(this, children));
	}

	private static Parser<List<SyntaxTreeNode>> listOfParsers(final String name, final List<Parser<SyntaxTreeNode>> p)
	{
		switch (p.size()) {
		case 0:
			return Scanners.pattern(new EmptyPatternImpl(), name)
					.retn(ImmutableList.of());
		case 1:
			return p.get(0).map(node -> ImmutableList.of(node));
		case 2:
			return Parsers.sequence(p.get(0), p.get(1), (n0, n1) -> ImmutableList.of(n0, n1));
		case 3:
			return Parsers.sequence(p.get(0), p.get(1), p.get(2),
					(n0, n1, n2) -> ImmutableList.of(n0, n1, n2));
		case 4:
			return Parsers.sequence(p.get(0), p.get(1), p.get(2), p.get(3),
					(n0, n1, n2, n3) -> ImmutableList.of(n0, n1, n2, n3));
		case 5:
			return Parsers.sequence(p.get(0), p.get(1), p.get(2), p.get(3), p.get(4),
					(n0, n1, n2, n3, n4) -> ImmutableList.of(n0, n1, n2, n3, n4));
		default:
		{
			final Parser<List<SyntaxTreeNode>> xs = listOfParsers(name, p.subList(0, 5));
			final Parser<List<SyntaxTreeNode>> ys = listOfParsers(name, p.subList(5, p.size()));
			return Parsers.sequence(xs, ys,
					(n0, n1) -> ImmutableList.<SyntaxTreeNode>builder().addAll(n0).addAll(n1).build());
		}
		}
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public List<SyntaxSymbol> childSymbols()
	{
		return childSymbols;
	}

	@Override
	public SyntaxTreeNode instantiate(final List<SyntaxTreeNode> children)
	{
		// Will throw an exception if the children list is not ok.
		return nodeFactory.list(this, children);
	}

	@Override
	public SyntaxTreeNode parse(final String text)
	{
		try
		{
			return parser.parse(text);
		}
		catch (final ParserException ex)
		{
			throw new SyntaxParserException(ex);
		}
	}

	@Override
	public Parser<SyntaxTreeNode> parser()
	{
		return parser;
	}

	@Override
	public SyntaxSymbolType type()
	{
		return SyntaxSymbolType.SEQUENCE;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append('(');
		boolean first = true;
		for (final SyntaxSymbol symbol : childSymbols)
		{
			if (!first)
			{
				sb.append(' ');
			}
			first = false;
			sb.append(symbol.name());
		}
		return sb.append(')').toString();
	}

}
