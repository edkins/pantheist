package restless.api.schema.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListSchemaResponseImpl.class)
public interface ListSchemaResponse
{
	@JsonProperty("childResources")
	List<ListSchemaItem> childResources();
}
