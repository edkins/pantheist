package io.pantheist.api.flatdir.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.DataAction;

@JsonDeserialize(as = ApiFlatDirFileImpl.class)
public interface ApiFlatDirFile
{
	@JsonProperty("dataAction")
	DataAction dataAction();

	@Nullable
	@JsonProperty("kindUrl")
	String kindUrl();
}
