package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = KindHookImpl.class)
public interface KindHook
{
	@JsonProperty("type")
	HookType type();

	@JsonProperty("plugin")
	String plugin();
}
