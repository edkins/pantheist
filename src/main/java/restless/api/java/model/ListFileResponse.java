package restless.api.java.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListFileResponseImpl.class)
public interface ListFileResponse
{
	@JsonProperty("childResources")
	List<ListFileItem> childResources();
}
