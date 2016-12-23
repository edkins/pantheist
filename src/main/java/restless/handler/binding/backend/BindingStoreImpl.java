package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import restless.common.util.MutableOptional;
import restless.handler.binding.model.HandlerType;
import restless.handler.binding.model.PathSpec;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.LockedFile;
import restless.handler.filesystem.backend.LockedTypedFile;
import restless.system.config.RestlessConfig;

final class BindingStoreImpl implements BindingStore
{
	private final FilesystemStore filesystem;
	private final RestlessConfig config;

	@Inject
	private BindingStoreImpl(final RestlessConfig config,
			final FilesystemStore filesystem)
	{
		this.config = checkNotNull(config);
		this.filesystem = checkNotNull(filesystem);
	}

	@Override
	public void initialize()
	{
		filesystem.initialize();
		filesystem.bindPath(bindingsPath());
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			if (!f.fileExits())
			{
				f.write(BindingSetImpl.empty());
			}
		}
	}

	@Override
	public void bind(final PathSpec pathSpec, final HandlerType handlerType, final String handlerPath)
	{
		switch (handlerType) {
		case filesystem:
			bindFilesystem(pathSpec, handlerPath);
			break;
		default:
			throw new UnsupportedOperationException("Unknown handler: " + handlerType);
		}
	}

	private void bindFilesystem(final PathSpec pathSpec, final String handlerPath)
	{
		final Binding binding = new BindingImpl(
				HandlerType.filesystem,
				pathSpec,
				UUID.randomUUID().toString(),
				handlerPath);
		registerBinding(binding);
		try
		{
			final FsPath path = filesystem.nonemptyPath(handlerPath);
			filesystem.bindPath(path);
			try (LockedFile f = filesystem.lock(path))
			{
				f.createDirectoryIfNotPresent();
			}
		}
		catch (final RuntimeException e)
		{
			deregisterBinding(binding);
			throw e;
		}
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
		final MutableOptional<Binding> point = MutableOptional.empty();
		for (final Binding binding : snapshot())
		{
			if (binding.pathSpec().contains(pathSpec))
			{
				point.add(binding);
			}
		}
		if (point.isPresent())
		{
			return functionsFor(pathSpec, point.get());
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

	private ManagementFunctions functionsFor(final PathSpec pathSpec, final Binding bindingPoint)
	{
		final PathSpec relativePathSpec = pathSpec.relativeTo(bindingPoint.pathSpec());
		switch (bindingPoint.handler()) {
		case filesystem:
			final FsPath path = filesystem.nonemptyPath(bindingPoint.handlerPath())
					.plusRelativePathSpec(relativePathSpec);
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
		return filesystem.nonemptyPath(config.fsBindingPath()).segment("bindings");
	}
}
