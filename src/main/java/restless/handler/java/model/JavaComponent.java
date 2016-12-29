package restless.handler.java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = JavaComponentImpl.class)
public interface JavaComponent
{
	@JsonProperty("isRoot")
	boolean isRoot();
}
