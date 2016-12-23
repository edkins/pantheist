package restless.handler.filesystem.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import restless.handler.binding.backend.ManagementFunctions;

public class HandlerFilesystemBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(FilesystemStore.class);

		install(new FactoryModuleBuilder()
				.implement(LockedFile.class, LockedFileImpl.class)
				.implement(ManagementFunctions.class, FilesystemManagementFunctionsImpl.class)
				.build(FilesystemFactory.class));
		bind(FilesystemStore.class).to(FilesystemStoreInterfaces.class);
		bind(FilesystemUnlock.class).to(FilesystemStoreInterfaces.class);
		bind(FilesystemStoreInterfaces.class).to(FilesystemStoreImpl.class).in(Scopes.SINGLETON);
	}

}
