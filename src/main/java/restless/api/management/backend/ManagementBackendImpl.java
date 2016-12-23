package restless.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import restless.api.management.model.ConfigRequest;
import restless.handler.binding.backend.BindingStore;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.PathSpecSegment;
import restless.handler.binding.model.PathSpecSegmentType;

final class ManagementBackendImpl implements ManagementBackend
{
	private final BindingModelFactory bindingFactory;
	private final BindingStore bindingStore;

	@Inject
	ManagementBackendImpl(final BindingModelFactory bindingFactory,
			final BindingStore bindingStore)
	{
		this.bindingFactory = checkNotNull(bindingFactory);
		this.bindingStore = checkNotNull(bindingStore);
	}

	@Override
	public PathSpec pathSpec(final String path)
	{
		final ImmutableList.Builder<PathSpecSegment> builder = ImmutableList.builder();
		for (final String seg : path.split("\\/"))
		{
			builder.add(segment(seg));
		}
		return bindingFactory.pathSpec(builder.build());
	}

	private PathSpecSegment segment(final String seg)
	{
		if (seg.startsWith("+"))
		{
			return bindingFactory.pathSpecSegment(PathSpecSegmentType.literal, seg.substring(1));
		}
		else
		{
			throw new UnsupportedOperationException("Currently only literal path segments supported");
		}
	}

	@Override
	public void putConfig(final PathSpec path, final ConfigRequest config)
	{
		bindingStore.bind(path, config.handler(), config.handlerPath());
	}

	@Override
	public void putData(final PathSpec path, final String data)
	{
		bindingStore.lookup(path).putString(data);
	}

	@Override
	public String getData(final PathSpec path)
	{
		return bindingStore.lookup(path).getString();
	}

}
