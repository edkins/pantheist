package io.pantheist.handler.nginx.parser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

import io.pantheist.handler.nginx.parser.StringHelpers;

public class StringHelpersTest
{
	@Test
	public void empty_hasNoIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd(""), is(Optional.empty()));
	}

	@Test
	public void justSpaces_hasNoIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("    "), is(Optional.empty()));
	}

	@Test
	public void word_hasNoIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello"), is(Optional.empty()));
	}

	@Test
	public void wordWithSpaces_hasNoIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello   "), is(Optional.empty()));
	}

	@Test
	public void wordWithNewLine_andNoSpaces_hasEmptyIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello\n"), is(Optional.of("")));
	}

	@Test
	public void wordWithNewLine_andSpacesBefore_butNoSpacesAfter_hasEmptyIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello   \n"), is(Optional.of("")));
	}

	@Test
	public void wordWithNewLine_andSpacesAfter_hasIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello\n  "), is(Optional.of("  ")));
	}

	@Test
	public void justNewLine_andSpacesAfter_hasIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("\n  "), is(Optional.of("  ")));
	}

	@Test
	public void wordWithNewLine_andTabsAfter_hasIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello\n\t\t"), is(Optional.of("\t\t")));
	}

	@Test
	public void wordWithNewLine_andMixtureOfSpacesAndTabsAfter_hasIndentation() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello  \n\t \t"), is(Optional.of("\t \t")));
	}

	@Test
	public void wordWithMultipleNewLinesAfter_hasIndentationFromLastSegment() throws Exception
	{
		assertThat(StringHelpers.indentationAtEnd("hello\n   \n     "), is(Optional.of("     ")));
	}

}
