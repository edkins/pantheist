package io.pantheist.handler.parser.generic;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

public class HandlerParserModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SyntaxSymbolFactory.class);
		install(new FactoryModuleBuilder()
				.implement(SyntaxSymbol.class, Names.named("regex"), SyntaxSymbolRegexImpl.class)
				.implement(SyntaxSymbol.class, Names.named("many"), SyntaxSymbolManyImpl.class)
				.implement(SyntaxSymbol.class, Names.named("sequence"), SyntaxSymbolSequenceImpl.class)
				.implement(SyntaxSymbol.class, Names.named("choice"), SyntaxSymbolChoiceImpl.class)
				.build(SyntaxSymbolFactory.class));

		expose(SyntaxTreeNodeFactory.class);
		install(new FactoryModuleBuilder()
				.implement(SyntaxTreeNode.class, Names.named("terminal"), SyntaxTreeNodeTerminalImpl.class)
				.implement(SyntaxTreeNode.class, Names.named("list"), SyntaxTreeNodeListImpl.class)
				.build(SyntaxTreeNodeFactory.class));
	}

}
