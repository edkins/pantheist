package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import restless.common.util.Make;

final class BindingSetImpl implements BindingSet
{
	private final ImmutableList<Binding> bindings;

	BindingSetImpl(@JsonProperty("bindings") final List<Binding> bindings)
	{
		checkNotNull(bindings);
		this.bindings = ImmutableList.copyOf(bindings);
	}

	static BindingSetImpl empty()
	{
		return new BindingSetImpl(ImmutableList.of());
	}

	@Override
	public List<Binding> bindings()
	{
		return bindings;
	}

	@Override
	public BindingSet plus(final Binding binding)
	{
		return new BindingSetImpl(Make.list(bindings, binding));
	}

	@Override
	public BindingSet minus(final Binding binding)
	{
		return new BindingSetImpl(Make.listWithSingleItemRemoved(bindings, binding));
	}

}
