package restless.handler.binding.backend;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.handler.binding.model.Binding;
import restless.handler.binding.model.ConfigId;

@JsonDeserialize(as = BindingSetImpl.class)
interface BindingSet
{
	@JsonProperty("bindings")
	List<Binding> bindings();

	/**
	 * Creates a new binding, and fails if one already exists with this id.
	 */
	@JsonIgnore
	void create(Binding binding);

	/**
	 * Replaces the binding with the given path spec with the one specified.
	 */
	@JsonIgnore
	void replace(Binding binding);

	/**
	 * Return the binding with the given id, or an empty result if there's none there.
	 */
	@JsonIgnore
	Optional<Binding> get(ConfigId configId);
}
