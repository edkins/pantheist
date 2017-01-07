package io.pantheist.handler.kind.model;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.annotations.IgnoredOnTheWayIn;
import io.pantheist.common.annotations.NotNullableOnTheWayOut;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.KindPresentation;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = KindImpl.class)
public interface Kind
{
	@NotNullableOnTheWayOut
	@Nullable
	@JsonProperty("kindId")
	String kindId();

	@JsonProperty("partOfSystem")
	boolean partOfSystem();

	@JsonProperty("schema")
	KindSchema schema();

	@Nullable
	@JsonProperty("specified")
	KindSpecification specified();

	@IgnoredOnTheWayIn
	@JsonProperty("computed")
	KindComputed computed();

	@Nullable
	@JsonProperty("presentation")
	KindPresentation presentation();

	@Nullable
	@JsonProperty("createAction")
	CreateAction createAction();

	@JsonProperty("deleteAction")
	DeleteAction deleteAction();

	@JsonProperty("listable")
	boolean listable();

	@Nullable
	@JsonProperty("affordances")
	List<Affordance> affordances();

	/**
	 * Convenience method for obtaining the parent kind ID specified in the schema, if any.
	 */
	@JsonIgnore
	Optional<String> parent();

	@JsonIgnore
	boolean hasParent(String parentId);

	/**
	 * Convenience method for checking this looks like a valid builtin kind, i.e. partOfSystem is true
	 * and there's no parent kind.
	 */
	@JsonIgnore
	boolean isBuiltinKind();

	/**
	 * Builtin kinds that also specify properties get registered in sql.
	 */
	@JsonIgnore
	boolean shouldRegisterInSql();

	@JsonIgnore
	void setKindId(String kindId);
}
