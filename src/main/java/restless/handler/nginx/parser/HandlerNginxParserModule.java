package restless.handler.nginx.parser;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

public class HandlerNginxParserModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(NginxSyntax.class);
		bind(NginxSyntax.class).to(NginxSyntaxImpl.class).in(Scopes.SINGLETON);

		install(new FactoryModuleBuilder()
				.implement(NginxRoot.class, NginxRootImpl.class)
				.implement(NginxWord.class, NginxWordImpl.class)
				.implement(NginxNameAndParameters.class, NginxNameAndParametersImpl.class)
				.implement(NginxBlock.class, Names.named("empty"), NginxBlockEmptyImpl.class)
				.implement(NginxBlock.class, Names.named("block"), NginxBlockImpl.class)
				.implement(NginxDirective.class, NginxDirectiveImpl.class)
				.implement(NginxCollection.class, NginxCollectionImpl.class)
				.build(NginxNodeFactory.class));
	}

}
