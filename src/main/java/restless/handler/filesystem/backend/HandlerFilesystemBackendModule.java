package restless.handler.filesystem.backend;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerFilesystemBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(FilesystemStore.class);
		bind(FilesystemStore.class).to(FilesystemStoreImpl.class).in(Scopes.SINGLETON);

		install(new FactoryModuleBuilder()
				.implement(FilesystemSnapshot.class, FilesystemSnapshotImpl.class)
				.build(FilesystemFactory.class));
	}

	@FilesystemLock
	@Provides
	Lock providesFilesystemLock()
	{
		return new ReentrantLock();
	}
}
