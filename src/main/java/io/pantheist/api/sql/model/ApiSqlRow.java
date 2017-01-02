package io.pantheist.api.sql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.DataAction;

@JsonDeserialize(as = ApiSqlRowImpl.class)
public interface ApiSqlRow
{
	@JsonProperty("dataAction")
	DataAction dataAction();
}
