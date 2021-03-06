package io.pantheist.api.java.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CreateAction;

@JsonDeserialize(as = ListJavaPkgResponseImpl.class)
public interface ListJavaPkgResponse
{
	@JsonProperty("childResources")
	List<ListJavaPkgItem> childResources();

	@JsonProperty("createAction")
	CreateAction createAction();

	@JsonProperty("bindingAction")
	BindingAction bindingAction();
}
