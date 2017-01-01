package io.pantheist.api.kind.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.CreateAction;

@JsonDeserialize(as = ListKindResponseImpl.class)
public interface ListKindResponse
{
	@JsonProperty("childResources")
	List<ListKindItem> childResources();

	@JsonProperty("createAction")
	CreateAction createAction();
}
