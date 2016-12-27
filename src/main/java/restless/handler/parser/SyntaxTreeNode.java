package restless.handler.parser;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Represents a node in the syntax tree. The original text can be reconstructed by calling toString().
 *
 * Note that it is mutable. You can change things and then call toString() to get a modified version of
 * your original text that is still compatible with the syntax.
 */
public interface SyntaxTreeNode
{
	SyntaxSymbol symbol();

	/**
	 * Add a child node to the end.
	 *
	 * @throws UnsupportedOperationException if the symbol type is not MANY
	 * @throws IllegalArgumentException if the node symbol does not correspond to our child symbol.
	 */
	void append(SyntaxTreeNode node);

	/**
	 * Removes a particular child node.
	 *
	 * @throws UnsupportedOperationException if the symbol type is not MANY
	 * @throws IndexOutOfBoundsException
	 */
	void remove(int index);

	/**
	 * Returns an immutable copy of the child nodes, if any.
	 */
	ImmutableList<SyntaxTreeNode> children();

	/**
	 * Parses the given text and replaces this node with the result.
	 * @throws SyntaxParserException if unable to parse
	 */
	void modify(String text);

	/**
	 * Returns the original text, or the modified version of the text if this node has been modified.
	 */
	@Override
	String toString();

	/**
	 * Appends this.toString() and returns the StringBuilder
	 */
	StringBuilder toStringBuilder(StringBuilder sb);

	/**
	 * Returns a debug representation of this node.
	 */
	StringBuilder debugToString(StringBuilder sb);

	/**
	 * Change the list of child nodes.
	 *
	 * @throws UnsupportedOperationException if the symbol type is TERMINAL
	 * @throws IllegalArgumentException if the list of nodes is not allowed for this symbol:
	 *  - if the symbol type is CHOICE
	 *     - the size of the list is not exactly 1
	 *     - or it's not one of the accepted symbols
	 *  - if the symbol type is SEQUENCE
	 *     - the size of the list disagrees with the symbol
	 *     - one of the symbols disagrees
	 *  - if the symbol type is MANY
	 *     - one of the symbols disagrees
	 */
	void changeChildren(List<SyntaxTreeNode> newChildren);
}
