package io.pantheist.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parser.Reference;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.error.ParserException;

import io.pantheist.handler.parser.generic.RegexPattern;

final class NginxSyntaxImpl implements NginxSyntax
{
	private final NginxNodeFactory nodeFactory;
	private final Parser<NginxRoot> parser;

	@Inject
	private NginxSyntaxImpl(final NginxNodeFactory nodeFactory)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.parser = createParser();
	}

	private Parser<String> singleChar(final char ch, final Parser<String> ws)
	{
		final Parser<String> character = Scanners.isChar(ch).source();
		return Parsers.sequence(character, ws, (a, b) -> a + b);
	}

	@Override
	public NginxRoot parse(final String text)
	{
		try
		{
			return parser.parse(text);
		}
		catch (final ParserException e)
		{
			throw new NginxParseException(e);
		}
	}

	private Parser<NginxRoot> createParser()
	{
		final Parser<String> ws = RegexPattern.parserForRegex("whitespace", "( |\\t|\\n|#.*\\n)*");
		final Parser<String> name = RegexPattern.parserForRegex("name", "[a-zA-Z_][a-zA-Z0-9_]*");
		final Parser<String> spaces = RegexPattern.parserForRegex("spaces", "( |\\t)*");
		final Parser<String> wordString = RegexPattern.parserFor("word", NginxWordImpl.PATTERN);
		final Parser<NginxWord> word = Parsers.sequence(wordString, spaces, nodeFactory::word);
		final Parser<String> openBrace = singleChar('{', ws);
		final Parser<String> closeBrace = singleChar('}', ws);
		final Parser<NginxNameAndParameters> nameAndParameters = Parsers.sequence(name, ws, word.many(),
				nodeFactory::nameAndParameters);

		final Reference<List<NginxDirective>> directivesRef = Parser.newReference();
		final Parser<NginxBlock> block = Parsers.sequence(openBrace, directivesRef.lazy(), closeBrace,
				nodeFactory::block);
		final Parser<NginxBlock> noBlock = singleChar(';', ws).map(nodeFactory::noBlock);

		final Parser<NginxDirective> directive = Parsers.sequence(nameAndParameters, Parsers.or(block, noBlock),
				nodeFactory::directive);

		final Parser<List<NginxDirective>> directives = directive.many();
		directivesRef.set(directives);

		return Parsers.sequence(ws, directives, nodeFactory::root);
	}

}
