package restless.glue.initializer;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.common.util.Cleanup;
import restless.handler.binding.backend.BindingStore;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.nginx.manage.NginxService;
import restless.system.server.RestlessServer;

final class InitializerImpl implements Initializer
{
	private final FilesystemStore filesystem;
	private final BindingStore bindingStore;
	private final NginxService nginxService;
	private final RestlessServer server;

	@Inject
	private InitializerImpl(final FilesystemStore filesystem,
			final BindingStore bindingStore,
			final NginxService nginxService,
			final RestlessServer server)
	{
		this.filesystem = checkNotNull(filesystem);
		this.bindingStore = checkNotNull(bindingStore);
		this.nginxService = checkNotNull(nginxService);
		this.server = checkNotNull(server);
	}

	@Override
	public void start()
	{
		filesystem.initialize();
		bindingStore.initialize();
		server.start();
	}

	@Override
	public void close()
	{
		Cleanup.run(nginxService::stop, server::stop);
	}

}
