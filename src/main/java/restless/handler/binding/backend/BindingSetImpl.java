package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherCollectors;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.ConfigId;

final class BindingSetImpl implements BindingSet
{
	private final BindingModelFactory bindingModelFactory;

	//State
	private final List<Binding> bindings;
	int counter;

	@Inject
	BindingSetImpl(
			final @JacksonInject BindingModelFactory bindingModelFactory,
			@Assisted @JsonProperty("bindings") final List<Binding> bindings,
			@Assisted("counter") @JsonProperty("counter") final int counter)
	{
		checkNotNull(bindings);
		this.bindingModelFactory = checkNotNull(bindingModelFactory);
		this.bindings = new ArrayList<>(bindings);
		this.counter = counter;
	}

	@Override
	public List<Binding> bindings()
	{
		return bindings;
	}

	@Override
	public void create(final Binding binding)
	{
		if (get(binding.configId()).isPresent())
		{
			throw new IllegalStateException("Binding already exists with id " + binding.configId());
		}
		bindings.add(binding);
	}

	@Override
	public void replace(final Binding binding)
	{
		for (int i = 0; i < bindings.size(); i++)
		{
			if (bindings.get(i).configId().equals(binding.configId()))
			{
				bindings.set(i, binding);
				return;
			}
		}
		throw new IllegalStateException("Binding does not exist with id " + binding.configId());
	}

	@Override
	public Optional<Binding> get(final ConfigId configId)
	{
		return bindings
				.stream()
				.filter(b -> b.configId().equals(configId))
				.collect(OtherCollectors.toOptional());
	}

	@Override
	public ConfigId nextUnusedId()
	{
		final ConfigId configId = bindingModelFactory.configId(String.valueOf(counter));
		if (get(configId).isPresent())
		{
			throw new IllegalStateException("configId specified by counter is already in use!");
		}
		counter++;
		return configId;
	}

	@Override
	public int counter()
	{
		return counter;
	}

	@Override
	public void remove(final ConfigId configId)
	{
		for (int i = 0; i < bindings.size(); i++)
		{
			if (bindings.get(i).configId().equals(configId))
			{
				bindings.remove(i);
				return;
			}
		}
	}

}
