package io.pantheist.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ReplaceActionImpl.class)
public interface ReplaceAction
{
	@JsonProperty("basicType")
	BasicContentType basicType();

	@JsonProperty("mimeType")
	String mimeType();
}
