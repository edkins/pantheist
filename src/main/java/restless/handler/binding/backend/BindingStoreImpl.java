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
import restless.handler.binding.model.ConfigId;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.PathSpecMatch;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.filesystem.backend.JsonSnapshot;

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
		final JsonSnapshot<BindingSet> file = file();
		if (!file.exists())
		{
			file.write(emptyBindingSet());
		}
	}

	private BindingSet emptyBindingSet()
	{
		return backendFactory.bindingSet(ImmutableList.of());
	}

	@Override
	public void changeConfig(final ConfigId pathSpec, final Function<Binding, Binding> fn)
	{
		final JsonSnapshot<BindingSet> file = file();
		final BindingSet bindingSet = file.read();
		final Binding oldBinding = bindingSet.get(pathSpec).get();
		final Binding newBinding = fn.apply(oldBinding);
		bindingSet.replace(newBinding);
		file.writeMutable();
	}

	@Override
	public BindingMatch lookup(final PathSpec pathSpec)
	{
		final MutableOptional<BindingMatch> result = MutableOptional.empty();
		for (final Binding binding : file().read().bindings())
		{
			final Optional<PathSpecMatch> maybeMatch = binding.pathSpec().match(pathSpec);
			if (maybeMatch.isPresent())
			{
				result.add(modelFactory.match(maybeMatch.get(), binding));
			}
		}
		return result.value().orElseGet(() -> emptyBindingMatch(pathSpec));
	}

	private BindingMatch emptyBindingMatch(final PathSpec pathSpec)
	{
		return modelFactory.match(modelFactory.pathSpecMatch(ImmutableList.of()), pathSpec.emptyBinding());
	}

	private JsonSnapshot<BindingSet> file()
	{
		return filesystem.jsonSnapshot(bindingsPath(), BindingSet.class);
	}

	@Override
	public List<Binding> snapshot()
	{
		return file().read().bindings();
	}

	private FsPath bindingsPath()
	{
		return filesystem.systemBucket().segment("bindings");
	}

	@Override
	public Binding exact(final ConfigId pathSpec)
	{
		return file().read().get(pathSpec).get();
	}

	@Override
	public ConfigId createConfig(final PathSpec pathSpec)
	{
		final JsonSnapshot<BindingSet> file = file();
		final Binding emptyBinding = pathSpec.emptyBinding();
		file.read().create(emptyBinding);
		file.writeMutable();
		return emptyBinding.configId();
	}
}
