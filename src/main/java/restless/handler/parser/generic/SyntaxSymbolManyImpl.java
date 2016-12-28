package restless.handler.parser.generic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class SyntaxSymbolManyImpl implements SyntaxSymbolJparsec
{
	private final SyntaxTreeNodeFactory nodeFactory;
	private final String name;
	private final SyntaxSymbol childSymbol;

	// Cached info
	private final List<SyntaxSymbol> childSymbols;
	private final Parser<SyntaxTreeNode> parser;

	@Inject
	public SyntaxSymbolManyImpl(final SyntaxTreeNodeFactory nodeFactory,
			@Assisted("name") final String name, @Assisted final SyntaxSymbol childSymbol)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.childSymbol = checkNotNull(childSymbol);
		this.parser = ((SyntaxSymbolJparsec) childSymbol)
				.parser()
				.many()
				.map(nodes -> this.nodeFactory.list(this, nodes));
		this.childSymbols = ImmutableList.of(childSymbol);
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
	public Parser<SyntaxTreeNode> parser()
	{
		return parser;
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
	public String toString()
	{
		return name + "=" + childSymbol.name() + "*";
	}

	@Override
	public SyntaxSymbolType type()
	{
		return SyntaxSymbolType.MANY;
	}
}
