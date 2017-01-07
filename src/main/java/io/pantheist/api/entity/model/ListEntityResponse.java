package io.pantheist.api.entity.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListEntityResponseImpl.class)
public interface ListEntityResponse
{
	@JsonProperty("childResources")
	List<ListEntityItem> childResources();
}
