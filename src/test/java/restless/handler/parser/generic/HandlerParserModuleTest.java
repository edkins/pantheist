package restless.handler.parser.generic;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;

import restless.handler.parser.generic.HandlerParserModule;
import restless.handler.parser.generic.SyntaxParserException;
import restless.handler.parser.generic.SyntaxSymbol;
import restless.handler.parser.generic.SyntaxSymbolFactory;
import restless.handler.parser.generic.SyntaxTreeNode;

public class HandlerParserModuleTest
{
	private SyntaxSymbolFactory sut;

	@Before
	public void setup()
	{
		sut = Guice.createInjector(new HandlerParserModule())
				.getInstance(SyntaxSymbolFactory.class);
	}

	@Test
	public void node_canInsert_andRemove() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol word = sut.many("word", letter);

		final SyntaxTreeNode hello = word.parse("hello");
		final SyntaxTreeNode p = letter.parse("p");
		hello.append(p);
		hello.remove(1);

		assertThat(hello.toString(), is("hllop"));
	}

	@Test
	public void node_cannotInsertWrongType() throws Exception
	{
		final SyntaxSymbolFactory sut = Guice.createInjector(new HandlerParserModule())
				.getInstance(SyntaxSymbolFactory.class);

		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol word = sut.many("word", letter);

		final SyntaxTreeNode hello = word.parse("hello");
		final SyntaxTreeNode asdf = word.parse("asdf");
		try
		{
			hello.append(asdf);
			fail("Expecting UnsupportedOperationException");
		}
		catch (final UnsupportedOperationException e)
		{
			// expected
		}
	}

	@Test
	public void sequence_canParse() throws Exception
	{
		final SyntaxSymbolFactory sut = Guice.createInjector(new HandlerParserModule())
				.getInstance(SyntaxSymbolFactory.class);

		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol seq = sut.sequence("seq", ImmutableList.of(letter, number, letter, number));

		final SyntaxTreeNode r2d2 = seq.parse("r2d2");

		assertThat(r2d2.toString(), is("r2d2"));
	}

	@Test
	public void longSequence_canParse() throws Exception
	{
		final SyntaxSymbolFactory sut = Guice.createInjector(new HandlerParserModule())
				.getInstance(SyntaxSymbolFactory.class);

		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol seq = sut.sequence("seq", ImmutableList.of(letter, letter, letter, letter, letter, number,
				number, number, letter, letter, letter));

		final SyntaxTreeNode stuff = seq.parse("abcde123abc");

		assertThat(stuff.toString(), is("abcde123abc"));
	}

	@Test
	public void partOfSequence_canModify() throws Exception
	{
		final SyntaxSymbolFactory sut = Guice.createInjector(new HandlerParserModule())
				.getInstance(SyntaxSymbolFactory.class);

		final SyntaxSymbol letters = sut.regex("letters", "[a-z]+");
		final SyntaxSymbol numbers = sut.regex("numbers", "[0-9]+");
		final SyntaxSymbol seq = sut.sequence("seq", ImmutableList.of(letters, numbers, letters));

		final SyntaxTreeNode tree = seq.parse("a2b");

		tree.children().get(1).modify("3003");
		assertThat(tree.toString(), is("a3003b"));
	}

	@Test
	public void entireSequence_canModify() throws Exception
	{
		final SyntaxSymbolFactory sut = Guice.createInjector(new HandlerParserModule())
				.getInstance(SyntaxSymbolFactory.class);

		final SyntaxSymbol letters = sut.regex("letters", "[a-z]+");
		final SyntaxSymbol numbers = sut.regex("numbers", "[0-9]+");
		final SyntaxSymbol seq = sut.sequence("seq", ImmutableList.of(letters, numbers, letters));

		final SyntaxTreeNode tree = seq.parse("a2b");

		tree.modify("zxcv3003qwer");
		assertThat(tree.toString(), is("zxcv3003qwer"));
	}

	@Test
	public void choice_canParse() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol choice = sut.choice("choice", ImmutableList.of(letter, number));

		final SyntaxTreeNode nine = choice.parse("9");

		assertThat(nine.toString(), is("9"));
	}

	@Test
	public void choiceChild_canModify() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol choice = sut.choice("choice", ImmutableList.of(letter, number));

		final SyntaxTreeNode nine = choice.parse("9");
		nine.children().get(0).modify("4");

		assertThat(nine.toString(), is("4"));
	}

	@Test
	public void choiceChild_cannotModifyToOtherThing() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol choice = sut.choice("choice", ImmutableList.of(letter, number));

		final SyntaxTreeNode nine = choice.parse("9");

		try
		{
			nine.children().get(0).modify("x");
			fail("Expecting SyntaxParserException");
		}
		catch (final SyntaxParserException e)
		{
			// expected
		}
	}

	@Test
	public void choiceNode_canModifyToOtherThing() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol choice = sut.choice("choice", ImmutableList.of(letter, number));

		final SyntaxTreeNode nine = choice.parse("9");
		nine.modify("x");

		assertThat(nine.toString(), is("x"));
	}

	@Test
	public void choiceSymbol_canInstantiate() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol choice = sut.choice("choice", ImmutableList.of(letter, number));

		final SyntaxTreeNode six = number.parse("6");
		final SyntaxTreeNode sixChoice = choice.instantiate(ImmutableList.of(six));

		assertThat(sixChoice.toString(), is("6"));
	}

	@Test
	public void choiceNode_canChangeChild() throws Exception
	{
		final SyntaxSymbol letter = sut.regex("letter", "[a-z]");
		final SyntaxSymbol number = sut.regex("number", "[0-9]");
		final SyntaxSymbol choice = sut.choice("choice", ImmutableList.of(letter, number));

		final SyntaxTreeNode dChoice = choice.parse("d");
		final SyntaxTreeNode six = number.parse("6");

		dChoice.changeChildren(ImmutableList.of(six));

		assertThat(dChoice.toString(), is("6"));
	}
}
