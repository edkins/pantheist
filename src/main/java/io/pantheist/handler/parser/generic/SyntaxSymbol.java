package io.pantheist.handler.parser.generic;

import java.util.List;

/**
 * Represents a symbol in the syntax.
 *
 * SyntaxSymbol implementations are not expected to override {@link Object#equals(Object)}.
 * Symbols are "the same" only if they are the same construction. Each syntax implementation
 * is expected to generate its symbols only once.
 *
 * SyntaxSymbol is immutable though.
 */
public interface SyntaxSymbol
{
	/**
	 * @return a name for this symbol, to help you understand what's going on.
	 */
	String name();

	SyntaxSymbolType type();

	/**
	 * Returns an immutable list of the child symbols.
	 *
	 * Will be empty if it's a terminal symbol. Will be a singleton if it's a MANY symbol.
	 */
	List<SyntaxSymbol> childSymbols();

	/**
	 * Create a SyntaxTreeNode instance for this symbol. This is exposed in order to let
	 * you create new syntax tree nodes and add them to parent nodes. You can also do this
	 * by calling parse(), but sometimes this way is more convenient.
	 *
	 * @throws UnsupportedOperationException if this is a terminal symbol
	 * @throws IllegalArgumentException if the list of children is incompatible with this symbol
	 */
	SyntaxTreeNode instantiate(List<SyntaxTreeNode> children);

	/**
	 * Parse the text and return a SyntaxTreeNode.
	 * @throws SyntaxParserException if unable to parse
	 */
	SyntaxTreeNode parse(String text);
}
