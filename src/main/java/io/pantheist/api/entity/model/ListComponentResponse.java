package io.pantheist.api.entity.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListComponentResponseImpl.class)
public interface ListComponentResponse
{
	@JsonProperty("childResources")
	List<ListComponentItem> childResources();
}
