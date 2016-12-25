package restless.glue.nginx.filesystem;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.handler.binding.backend.BindingStore;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.PathSpec;
import restless.handler.nginx.manage.NginxService;
import restless.handler.nginx.model.NginxConfig;
import restless.handler.nginx.model.NginxLocation;
import restless.handler.nginx.model.NginxServer;

final class NginxFilesystemGlueImpl implements NginxFilesystemGlue
{
	private final NginxService nginxService;
	private final BindingStore bindingStore;
	private final BindingModelFactory bindingFactory;

	@Inject
	private NginxFilesystemGlueImpl(
			final NginxService nginxService,
			final BindingStore bindingStore,
			final BindingModelFactory bindingFactory)
	{
		this.nginxService = checkNotNull(nginxService);
		this.bindingStore = checkNotNull(bindingStore);
		this.bindingFactory = checkNotNull(bindingFactory);
	}

	@Override
	public NginxConfig nginxConf()
	{
		final NginxConfig nginx = nginxService.newConfig();
		final NginxServer server = nginx.httpServer();

		for (final Binding binding : bindingStore.snapshot())
		{
			switch (binding.handler().type()) {
			case filesystem:
				addFilesystemLocation(server, binding);
				break;
			default:
				throw new UnsupportedOperationException("Unrecognized handler: " + binding.handler());
			}
		}
		return nginx;
	}

	private void addFilesystemLocation(final NginxServer server, final Binding binding)
	{
		final NginxLocation location;
		final PathSpec path = binding.pathSpec();
		switch (path.classify()) {
		case EXACT:
			location = server.addLocationEquals(path.literalString());
			break;
		case PREFIX:
			location = server.addLocation(path.minus(bindingFactory.multi()).literalString());
			break;
		case PREFIX_STAR:
			// TODO: this is not correct. It will match anything in the directory, not just one level deep as requested.
			location = server.addLocation(path.minus(bindingFactory.star()).literalString());
			break;
		default:
			throw new UnsupportedOperationException("Cannot handle this kind of path");
		}
		location.alias().giveDirPath(binding.handler().filesystemBucket());
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
