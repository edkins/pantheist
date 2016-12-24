package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherCollectors;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.PathSpec;

final class BindingSetImpl implements BindingSet
{
	private final ImmutableList<Binding> bindings;
	private final BindingModelFactory modelFactory;
	private final BindingBackendFactory backendFactory;

	@Inject
	BindingSetImpl(@JacksonInject final BindingModelFactory modelFactory,
			@JacksonInject final BindingBackendFactory backendFactory,
			@Assisted @JsonProperty("bindings") final List<Binding> bindings)
	{
		checkNotNull(bindings);
		this.bindings = ImmutableList.copyOf(bindings);
		this.modelFactory = checkNotNull(modelFactory);
		this.backendFactory = checkNotNull(backendFactory);
	}

	@Override
	public List<Binding> bindings()
	{
		return bindings;
	}

	@Override
	public BindingSet put(final Binding binding)
	{
		return backendFactory.bindingSet(bindings
				.stream()
				.filter(b -> !b.pathSpec().equals(binding.pathSpec()))
				.collect(OtherCollectors.toListBuilder())
				.add(binding)
				.build());
	}

	@Override
	public Binding get(final PathSpec pathSpec)
	{
		return bindings
				.stream()
				.filter(b -> b.pathSpec().equals(pathSpec))
				.collect(OtherCollectors.toOptional())
				.orElse(emptyBinding(pathSpec));
	}

	private Binding emptyBinding(final PathSpec pathSpec)
	{
		return modelFactory.binding(pathSpec, modelFactory.emptyHandler(), modelFactory.emptySchema());
	}

}
