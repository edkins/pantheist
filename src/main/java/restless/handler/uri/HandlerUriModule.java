package restless.handler.uri;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerUriModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(UrlTranslation.class);
		bind(UrlTranslation.class).to(UrlTranslationImpl.class).in(Scopes.SINGLETON);

		expose(HandlerUriModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListClassifierItem.class, ListClassifierItemImpl.class)
				.build(HandlerUriModelFactory.class));
	}

}
