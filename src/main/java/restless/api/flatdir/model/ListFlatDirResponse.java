package restless.api.flatdir.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListFlatDirResponseImpl.class)
public interface ListFlatDirResponse
{
	@JsonProperty("childResources")
	List<ListFlatDirItem> childResources();
}
