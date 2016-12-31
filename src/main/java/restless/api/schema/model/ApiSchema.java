package restless.api.schema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.common.api.model.DataAction;
import restless.common.api.model.DeleteAction;

@JsonDeserialize(as = ApiSchemaImpl.class)
public interface ApiSchema
{
	@JsonProperty("dataAction")
	DataAction dataAction();

	@JsonProperty("deleteAction")
	DeleteAction deleteAction();
}
