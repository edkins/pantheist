package io.pantheist.api.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.api.model.Presentation;
import io.pantheist.common.api.model.ReplaceAction;
import io.pantheist.handler.kind.model.KindSchema;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = ApiKindImpl.class)
public interface ApiKind extends ListClassifierResponse
{
	@Nullable
	@JsonProperty("kindId")
	String kindId(); // optional on put requests, but if present must agree with where you're putting it.

	@JsonProperty("schema")
	KindSchema schema();

	/**
	 * Return whether this is used by the system itself.
	 *
	 * For now this is just a tag that is remembered but doesn't do anything.
	 */
	@JsonProperty("partOfSystem")
	boolean partOfSystem();

	@JsonProperty("replaceAction")
	ReplaceAction replaceAction();

	@Nullable
	@JsonProperty("instancePresentation")
	Presentation instancePresentation();
}
