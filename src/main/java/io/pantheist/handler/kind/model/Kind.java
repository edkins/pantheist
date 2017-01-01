package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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

	/**
	 * If two kinds match, the higher number will be chosen.
	 */
	@JsonProperty("precedence")
	int precedence();
}
