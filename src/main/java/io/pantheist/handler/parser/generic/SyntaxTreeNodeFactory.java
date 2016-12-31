package io.pantheist.handler.parser.generic;

import java.util.List;

import javax.inject.Named;

import com.google.inject.assistedinject.Assisted;

interface SyntaxTreeNodeFactory
{
	@Named("terminal")
	SyntaxTreeNode terminal(SyntaxSymbol symbol, @Assisted("text") String text);

	@Named("list")
	SyntaxTreeNode list(SyntaxSymbol symbol, List<SyntaxTreeNode> children);
}
