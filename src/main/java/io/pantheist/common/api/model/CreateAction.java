package io.pantheist.common.api.model;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = CreateActionImpl.class)
public interface CreateAction
{
	@JsonProperty("basicType")
	BasicContentType basicType();

	@JsonProperty("mimeType")
	String mimeType();

	@Nullable
	@JsonProperty("additionalStructure")
	List<AdditionalStructureItem> additionalStructure();
}
