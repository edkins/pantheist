package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.util.OtherPreconditions;

final class KindHookImpl implements KindHook
{
	private final HookType type;
	private final String plugin;

	public KindHookImpl(
			@JsonProperty("type") final HookType type,
			@JsonProperty("plugin") final String plugin)
	{
		this.type = checkNotNull(type);
		this.plugin = OtherPreconditions.checkNotNullOrEmpty(plugin);
	}

	@Override
	public HookType type()
	{
		return type;
	}

	@Override
	public String plugin()
	{
		return plugin;
	}

}
