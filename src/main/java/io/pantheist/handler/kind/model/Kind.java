package io.pantheist.handler.kind.model;

import java.util.List;

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

	@JsonProperty("level")
	KindLevel level();

	@JsonProperty("discoverable")
	boolean discoverable();

	@Nullable
	@JsonProperty("java")
	JavaClause java();

	@JsonProperty("partOfSystem")
	boolean partOfSystem();

	@JsonProperty("subKindOf")
	List<String> subKindOf();

	@Nullable
	@JsonProperty("instancePresentation")
	Presentation instancePresentation();
}
