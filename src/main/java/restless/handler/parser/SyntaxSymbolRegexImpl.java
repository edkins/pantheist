package restless.handler.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.error.ParserException;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class SyntaxSymbolRegexImpl implements SyntaxSymbolJparsec
{
	private final SyntaxTreeNodeFactory nodeFactory;
	private final String name;
	private final String regex;
	private final Parser<SyntaxTreeNode> parser;

	@Inject
	private SyntaxSymbolRegexImpl(final SyntaxTreeNodeFactory nodeFactory,
			@Assisted("name") final String name,
			@Assisted("regex") final String regex)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.regex = checkNotNull(regex);
		this.parser = Scanners.pattern(new RegexPatternImpl(regex), name).source()
				.map(text -> this.nodeFactory.terminal(this, text));
	}

	@Override
	public List<SyntaxSymbol> childSymbols()
	{
		return ImmutableList.of();
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public Parser<SyntaxTreeNode> parser()
	{
		return parser;
	}

	@Override
	public String toString()
	{
		return name + "=/" + regex + "/";
	}

	@Override
	public SyntaxTreeNode instantiate(final List<SyntaxTreeNode> children)
	{
		throw new UnsupportedOperationException("instantiate() is unsupported for terminal symbol " + this);
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
	public SyntaxSymbolType type()
	{
		return SyntaxSymbolType.TERMINAL;
	}

}
