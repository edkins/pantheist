package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import restless.common.util.MutableOptional;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingMatch;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.PathSpecMatch;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.LockedTypedFile;

final class BindingStoreImpl implements BindingStore
{
	private final FilesystemStore filesystem;
	private final BindingModelFactory modelFactory;
	private final BindingBackendFactory backendFactory;

	@Inject
	private BindingStoreImpl(final FilesystemStore filesystem,
			final BindingModelFactory modelFactory,
			final BindingBackendFactory backendFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.modelFactory = checkNotNull(modelFactory);
		this.backendFactory = checkNotNull(backendFactory);
	}

	@Override
	public void initialize()
	{
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			if (!f.fileExits())
			{
				f.write(emptyBindingSet());
			}
		}
	}

	private BindingSet emptyBindingSet()
	{
		return backendFactory.bindingSet(ImmutableList.of());
	}

	@Override
	public void changeConfig(final PathSpec pathSpec, final Function<Binding, Binding> fn)
	{
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			// Might make more sense to use mutable objects here.

			final BindingSet oldBindingSet = f.read();
			final Binding oldBinding = oldBindingSet.get(pathSpec);
			final Binding newBinding = fn.apply(oldBinding);
			final BindingSet newBindingSet = oldBindingSet.put(newBinding);
			f.write(newBindingSet);
		}
	}

	@Override
	public Optional<BindingMatch> lookup(final PathSpec pathSpec)
	{
		final MutableOptional<BindingMatch> result = MutableOptional.empty();
		for (final Binding binding : snapshot())
		{
			final Optional<PathSpecMatch> maybeMatch = binding.pathSpec().match(pathSpec);
			if (maybeMatch.isPresent())
			{
				result.add(modelFactory.match(maybeMatch.get(), binding));
			}
		}
		return result.value();
	}

	@Override
	public List<Binding> snapshot()
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
	public Binding exact(final PathSpec pathSpec)
	{
		try (LockedTypedFile<BindingSet> f = lockBindings())
		{
			return f.read().get(pathSpec);
		}
	}
}
