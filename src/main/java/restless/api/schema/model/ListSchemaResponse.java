package restless.api.schema.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.common.api.model.CreateAction;

@JsonDeserialize(as = ListSchemaResponseImpl.class)
public interface ListSchemaResponse
{
	@JsonProperty("childResources")
	List<ListSchemaItem> childResources();

	@JsonProperty("createAction")
	CreateAction createAction();
}
