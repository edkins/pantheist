package io.pantheist.api.sql.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListSqlTableResponseImpl.class)
public interface ListSqlTableResponse
{
	@JsonProperty("childResources")
	List<ListSqlTableItem> childResources();
}
