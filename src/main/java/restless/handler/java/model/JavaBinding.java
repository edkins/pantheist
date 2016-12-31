package restless.handler.java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = JavaBindingImpl.class)
public interface JavaBinding
{
	@JsonProperty("location")
	String location();
}
