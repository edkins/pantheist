package restless.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = BindingActionImpl.class)
public interface BindingAction
{
	@JsonProperty("url")
	String url();
}
