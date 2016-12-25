package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherCollectors;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.ConfigId;

final class BindingSetImpl implements BindingSet
{
	//State
	private final List<Binding> bindings;

	@Inject
	BindingSetImpl(
			@Assisted @JsonProperty("bindings") final List<Binding> bindings)
	{
		checkNotNull(bindings);
		this.bindings = new ArrayList<>(bindings);
	}

	@Override
	public List<Binding> bindings()
	{
		return bindings;
	}

	@Override
	public void put(final Binding binding)
	{
		for (int i = 0; i < bindings.size(); i++)
		{
			if (bindings.get(i).pathSpec().equals(binding.pathSpec()))
			{
				bindings.set(i, binding);
				return;
			}
		}
		bindings.add(binding);
	}

	@Override
	public Binding get(final ConfigId pathSpec)
	{
		return bindings
				.stream()
				.filter(b -> b.configId().equals(pathSpec))
				.collect(OtherCollectors.toOptional())
				.orElseGet(pathSpec::emptyBinding);
	}

}
