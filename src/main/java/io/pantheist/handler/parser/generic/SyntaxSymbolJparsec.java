package io.pantheist.handler.parser.generic;

import org.codehaus.jparsec.Parser;

/**
 * Internal interface which exposes JParsec parser. All SyntaxSymbol instances secretly
 * implement this.
 */
interface SyntaxSymbolJparsec extends SyntaxSymbol
{
	Parser<SyntaxTreeNode> parser();
}
