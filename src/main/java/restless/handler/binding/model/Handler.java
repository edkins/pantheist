package restless.handler.binding.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = HandlerImpl.class)
public interface Handler
{
	@JsonProperty("type")
	HandlerType type();

	/**
	 * Where relevant, this refers to some path that the resources will be bound to.
	 *
	 * Used for: resource_files
	 */
	@Nullable
	@JsonProperty("handlerPath")
	String handlerPath();
}
