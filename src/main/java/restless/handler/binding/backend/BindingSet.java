package restless.handler.binding.backend;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = BindingSetImpl.class)
interface BindingSet
{
	@JsonProperty("bindings")
	List<Binding> bindings();

	@JsonIgnore
	BindingSet plus(Binding binding);

	@JsonIgnore
	BindingSet minus(Binding binding);
}
