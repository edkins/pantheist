package restless.api.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListComponentItemImpl.class)
public interface ListComponentItem
{
	@JsonProperty("componentId")
	String componentId();
}
