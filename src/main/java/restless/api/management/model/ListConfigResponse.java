package restless.api.management.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListConfigResponseImpl.class)
public interface ListConfigResponse
{
	@JsonProperty("childResources")
	List<ListConfigItem> childResources();
}
