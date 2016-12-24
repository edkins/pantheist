package restless.handler.binding.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = BindingImpl.class)
public interface Binding
{
	@JsonProperty("pathSpec")
	PathSpec pathSpec();

	@JsonProperty("handler")
	Handler handler();

	@JsonProperty("schema")
	Schema schema();

	@Nullable
	@JsonProperty("jerseyClass")
	String jerseyClass();
}
