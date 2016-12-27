package restless.glue.nginx.filesystem;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.handler.binding.backend.BindingStore;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.Handler;
import restless.handler.binding.model.PathSpec;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.nginx.manage.NginxService;
import restless.handler.nginx.model.NginxConfig;
import restless.handler.nginx.model.NginxLocation;
import restless.handler.nginx.model.NginxServer;

final class NginxFilesystemGlueImpl implements NginxFilesystemGlue
{
	private final NginxService nginxService;
	private final BindingStore bindingStore;
	private final BindingModelFactory bindingFactory;
	FilesystemStore filesystemStore;

	@Inject
	private NginxFilesystemGlueImpl(
			final NginxService nginxService,
			final BindingStore bindingStore,
			final BindingModelFactory bindingFactory,
			final FilesystemStore filesystemStore)
	{
		this.nginxService = checkNotNull(nginxService);
		this.bindingStore = checkNotNull(bindingStore);
		this.bindingFactory = checkNotNull(bindingFactory);
		this.filesystemStore = checkNotNull(filesystemStore);
	}

	@Override
	public NginxConfig nginxConf()
	{
		final NginxConfig nginx = nginxService.newConfig();
		nginx.http().root().giveDirPath(filesystemStore.srvBucket());

		final NginxServer server = nginx.httpServer();

		for (final Binding binding : bindingStore.listBindings())
		{
			addBindingLocation(server, binding);
		}
		return nginx;
	}

	private void addBindingLocation(final NginxServer server, final Binding binding)
	{
		final PathSpec path = binding.pathSpec();
		final NginxLocation location;
		switch (binding.handler().type()) {
		case empty:
			// ignore it
			break;
		case filesystem:
			addLocationForPath(server, path); // no further configuration required - files will be served relative to root
			break;
		case external_files:
			location = addLocationForPath(server, path);
			location.alias().giveAbsoluteDirPath(binding.handler().handlerPath());
			break;
		default:
			throw new UnsupportedOperationException("Unrecognized handler: " + binding.handler());
		}
	}

	private FsPath resourceFsPath(final Handler handler)
	{
		return filesystemStore.systemBucket().segment("resource-files")
				.slashSeparatedSegments(handler.handlerPath());
	}

	private NginxLocation addLocationForPath(final NginxServer server, final PathSpec path)
	{
		switch (path.classify()) {
		case EXACT:
			return server.addLocationEquals(path.literalString());
		case PREFIX:
			return server.addLocation(path.minus(bindingFactory.multi()).literalString());
		default:
			throw new UnsupportedOperationException("Cannot handle this kind of path");
		}
	}

	@Override
	public void startStopOrRestart()
	{
		final NginxConfig conf = nginxConf();
		if (conf.isEmpty())
		{
			nginxService.stop();
		}
		else
		{
			nginxService.configureAndStart(conf);
		}
	}
}
