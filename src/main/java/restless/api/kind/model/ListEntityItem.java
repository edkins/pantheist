package restless.api.kind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListEntityItemImpl.class)
public interface ListEntityItem
{
	@JsonProperty("entityId")
	String entityId();

	@JsonProperty("discovered")
	boolean discovered();
}
