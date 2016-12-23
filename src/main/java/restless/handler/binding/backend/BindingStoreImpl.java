package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import restless.common.util.MutableOptional;
import restless.handler.binding.model.HandlerType;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.PathSpecMatch;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.LockedFile;
import restless.handler.filesystem.backend.LockedTypedFile;
import restless.handler.nginx.manage.NginxService;

final class BindingStoreImpl implements BindingStore
{
	private final FilesystemStore filesystem;
	private final NginxService nginxService;

	@Inject
	private BindingStoreImpl(final FilesystemStore filesystem, final NginxService nginxService)
	{
		this.filesystem = checkNotNull(filesystem);
		this.nginxService = checkNotNull(nginxService);
	}

	@Override
	public void initialize()
	{
		filesystem.initialize();
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			if (!f.fileExits())
			{
				f.write(BindingSetImpl.empty());
			}
		}
	}

	@Override
	public void bind(final PathSpec pathSpec, final HandlerType handlerType)
	{
		switch (handlerType) {
		case filesystem:
			bindFilesystem(pathSpec);
			break;
		default:
			throw new UnsupportedOperationException("Unknown handler: " + handlerType);
		}
	}

	private void bindFilesystem(final PathSpec pathSpec)
	{
		final FsPath path = filesystem.newBucket(pathSpec.nameHint());
		try (LockedFile f = filesystem.lock(path))
		{
			f.createDirectoryIfNotPresent();
		}
		final Binding binding = new BindingImpl(
				HandlerType.filesystem,
				pathSpec,
				UUID.randomUUID().toString(),
				path.toString());
		registerBinding(binding);
		restartNginx();
	}

	private void deregisterBinding(final Binding binding)
	{
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			f.write(f.read().minus(binding));
		}

	}

	private void registerBinding(final Binding binding)
	{
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			f.write(f.read().plus(binding));
		}
	}

	@Override
	public ManagementFunctions lookup(final PathSpec pathSpec)
	{
		final MutableOptional<Binding> binding = MutableOptional.empty();
		final MutableOptional<PathSpecMatch> match = MutableOptional.empty();
		for (final Binding candidate : snapshot())
		{
			final Optional<PathSpecMatch> maybeMatch = candidate.pathSpec().match(pathSpec);
			if (maybeMatch.isPresent())
			{
				binding.add(candidate);
				match.add(maybeMatch);
			}
		}
		if (binding.isPresent())
		{
			return functionsFor(pathSpec, binding.get(), match.get());
		}
		else
		{
			return emptyFunctions();
		}
	}

	private ManagementFunctions emptyFunctions()
	{
		return new EmptyManagementFunctionsImpl();
	}

	private ManagementFunctions functionsFor(final PathSpec pathSpec,
			final Binding bindingPoint,
			final PathSpecMatch match)
	{
		switch (bindingPoint.handler()) {
		case filesystem:
			final FsPath path = filesystem
					.fromBucketName(bindingPoint.handlerPath())
					.withPathSegments(match.nonLiteralChunk());
			return filesystem.manage(path);
		default:
			throw new UnsupportedOperationException("Unrecognized handler: " + bindingPoint.handler());
		}
	}

	private List<Binding> snapshot()
	{
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			return f.read().bindings();
		}
	}

	private LockedTypedFile<BindingSet> lockBindings()
	{
		final FsPath path = bindingsPath();
		return filesystem.lockJson(path, BindingSet.class);
	}

	private FsPath bindingsPath()
	{
		return filesystem.systemBucket().segment("bindings");
	}

	@Override
	public void stop()
	{
		nginxService.stop();
	}

	private void restartNginx()
	{
		nginxService.configureAndStart();
	}
}
