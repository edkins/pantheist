package io.pantheist.handler.kind.model;

import java.util.Optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
}
