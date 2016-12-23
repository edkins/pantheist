package restless.handler.binding.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.handler.binding.model.HandlerType;
import restless.handler.binding.model.PathSpec;

@JsonDeserialize(as = BindingImpl.class)
interface Binding
{
	@JsonProperty("handler")
	HandlerType handler();

	@JsonProperty("handlerPath")
	String handlerPath();

	@JsonProperty("pathSpec")
	PathSpec pathSpec();

	@JsonProperty("bindingId")
	String bindingId();
}
