package restless.api.java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ApiJavaBindingImpl.class)
public interface ApiJavaBinding
{
	@JsonProperty("location")
	String location();
}
