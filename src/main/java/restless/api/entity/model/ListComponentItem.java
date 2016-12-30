package restless.api.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListComponentItemImpl.class)
public interface ListComponentItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("componentId")
	String componentId();
}
