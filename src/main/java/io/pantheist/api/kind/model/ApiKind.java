package io.pantheist.api.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.annotations.NotNullableOnTheWayOut;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.KindPresentation;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.handler.kind.model.KindSchema;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = ApiKindImpl.class)
public interface ApiKind extends ListClassifierResponse
{
	@NotNullableOnTheWayOut
	@JsonProperty("url")
	String url();

	@NotNullableOnTheWayOut
	@JsonProperty("kindUrl")
	String kindUrl();

	@NotNullableOnTheWayOut
	@Nullable
	@JsonProperty("kindId")
	String kindId();

	@JsonProperty("schema")
	KindSchema schema();

	/**
	 * Return whether this is used by the system itself.
	 *
	 * For now this is just a tag that is remembered but doesn't do anything.
	 */
	@JsonProperty("partOfSystem")
	boolean partOfSystem();

	@NotNullableOnTheWayOut
	@JsonProperty("dataAction")
	DataAction dataAction();

	@Nullable
	@JsonProperty("presentation")
	KindPresentation presentation();

	@Nullable
	@JsonProperty("createAction")
	CreateAction createAction();

	@Nullable
	@JsonProperty("deleteAction")
	DeleteAction deleteAction();
}
