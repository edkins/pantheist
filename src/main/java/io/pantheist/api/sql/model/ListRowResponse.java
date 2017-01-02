package io.pantheist.api.sql.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListRowResponseImpl.class)
public interface ListRowResponse
{
	@JsonProperty("childResources")
	List<ListRowItem> childResources();
}
