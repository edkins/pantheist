package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.Presentation;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = KindImpl.class)
public interface Kind
{
	@JsonProperty("kindId")
	String kindId();

	@JsonProperty("partOfSystem")
	boolean partOfSystem();

	@JsonProperty("schema")
	KindSchema schema();

	@Nullable
	@JsonProperty("instancePresentation")
	Presentation instancePresentation();
}
