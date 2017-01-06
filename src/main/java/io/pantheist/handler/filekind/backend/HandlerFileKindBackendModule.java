package io.pantheist.handler.filekind.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class HandlerFileKindBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(FileKindHandler.class);
		bind(FileKindHandler.class).to(FileKindHandlerImpl.class).in(Scopes.SINGLETON);
	}

}
