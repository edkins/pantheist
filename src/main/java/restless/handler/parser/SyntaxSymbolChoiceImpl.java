package restless.handler.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.error.ParserException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class SyntaxSymbolChoiceImpl implements SyntaxSymbolJparsec
{
	private final String name;
	private final List<SyntaxSymbol> childSymbols;
	private final SyntaxTreeNodeFactory nodeFactory;
	// Cached info
	private final Parser<SyntaxTreeNode> parser;

	@Inject
	private SyntaxSymbolChoiceImpl(
			final SyntaxTreeNodeFactory nodeFactory,
			@Assisted("name") final String name,
			@Assisted final List<SyntaxSymbol> childSymbols)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.childSymbols = ImmutableList.copyOf(childSymbols);

		this.parser = Parsers
				.or(Lists.transform(childSymbols, sym -> ((SyntaxSymbolJparsec) sym).parser()))
				.map(node -> nodeFactory.list(this, ImmutableList.of(node)));
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public SyntaxSymbolType type()
	{
		return SyntaxSymbolType.CHOICE;
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
				sb.append('|');
			}
			first = false;
			sb.append(symbol.name());
		}
		return sb.append(')').toString();
	}
}
