package restless.api.management.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.handler.binding.model.HandlerType;

@JsonDeserialize(as = ConfigRequestImpl.class)
public interface ConfigRequest
{
	@JsonProperty("handler")
	HandlerType handler();

	@Nullable
	@JsonProperty("handlerPath")
	String handlerPath();
}
