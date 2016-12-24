package restless.handler.binding.backend;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.handler.binding.model.Binding;
import restless.handler.binding.model.PathSpec;

@JsonDeserialize(as = BindingSetImpl.class)
interface BindingSet
{
	@JsonProperty("bindings")
	List<Binding> bindings();

	/**
	 * Replaces the binding with the given path spec with the one specified.
	 *
	 * If the binding is empty then it might get removed from the list.
	 */
	@JsonIgnore
	BindingSet put(Binding binding);

	/**
	 * Return the binding at the given path spec, or an empty binding if there's none there.
	 */
	@JsonIgnore
	Binding get(PathSpec pathSpec);
}
