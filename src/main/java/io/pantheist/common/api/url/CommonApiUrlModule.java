package io.pantheist.common.api.url;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class CommonApiUrlModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(UrlTranslation.class);
		bind(UrlTranslation.class).to(UrlTranslationImpl.class).in(Scopes.SINGLETON);
	}

}
