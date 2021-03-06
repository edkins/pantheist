package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = KindComputedImpl.class)
public interface KindComputed
{
	@Nullable
	@JsonProperty("mimeType")
	String mimeType();

	@Nullable
	@JsonProperty("handler")
	KindHandler handler();

	@Nullable
	@JsonProperty("childKindIds")
	List<String> childKindIds();

	@JsonProperty("hooks")
	List<KindHook> hooks();

	/**
	 * Not sure this concept makes sense.
	 *
	 * Currently it's used to represent the kinds whose instances are available
	 * under the entity hierarchy.
	 */
	@Deprecated
	@JsonProperty("isEntityKind")
	boolean isEntityKind();

	@JsonIgnore
	void setMimeType(String mimeType);

	@JsonIgnore
	void setHandler(KindHandler handler);

	@Deprecated
	@JsonIgnore
	void setEntityKind();

	@JsonIgnore
	void clear();

	@JsonIgnore
	void setChildKindIds(List<String> childKindIds);

	/**
	 * Add hooks to the beginning. (The idea is that the parent kind gets in there first).
	 */
	@JsonIgnore
	void addHooksToBeginning(@Nullable List<KindHook> hooks);
}
