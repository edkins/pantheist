package restless.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = DataActionImpl.class)
public interface DataAction
{
	@JsonProperty("basicType")
	BasicContentType basicType();

	@JsonProperty("mimeType")
	String mimeType();

	@JsonProperty("canPut")
	boolean canPut();
}
