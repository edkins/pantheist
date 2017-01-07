package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import io.pantheist.common.util.Make;

final class KindComputedImpl implements KindComputed
{
	private String mimeType;
	private KindHandler handler;
	private boolean isEntityKind;
	private ImmutableList<String> childKindIds;
	private ImmutableList<KindHook> hooks;

	private KindComputedImpl(
			@Nullable @JsonProperty("mimeType") final String mimeType,
			@Nullable @JsonProperty("handler") final KindHandler handler,
			@Deprecated @JsonProperty("isEntityKind") final boolean isEntityKind,
			@Nullable @JsonProperty("childKindIds") final List<String> childKindIds,
			@Nullable @JsonProperty("hooks") final List<KindHook> hooks)
	{
		this.mimeType = mimeType;
		this.handler = handler;
		this.isEntityKind = isEntityKind;
		this.childKindIds = Make.copyOfNullable(childKindIds);
		this.hooks = Make.emptyIfNullable(hooks);
	}

	/**
	 * Arbitrary because someone will call clear() later.
	 */
	static KindComputed arbitrary()
	{
		return new KindComputedImpl(null, null, false, null, ImmutableList.of());
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}

	@Override
	public KindHandler handler()
	{
		return handler;
	}

	@Override
	public boolean isEntityKind()
	{
		return isEntityKind;
	}

	@Override
	public void setMimeType(final String mimeType)
	{
		this.mimeType = mimeType;
	}

	@Override
	public void setHandler(final KindHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void setEntityKind()
	{
		this.isEntityKind = true;
	}

	@Override
	public void clear()
	{
		this.mimeType = null;
		this.handler = null;
		this.isEntityKind = false;
		this.childKindIds = null;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("mimeType", mimeType)
				.add("handler", handler)
				.add("isEntityKind", isEntityKind)
				.add("childKindIds", childKindIds)
				.add("hooks", hooks)
				.toString();
	}

	@Override
	public List<String> childKindIds()
	{
		return childKindIds;
	}

	@Override
	public void setChildKindIds(final List<String> childKindIds)
	{
		this.childKindIds = ImmutableList.copyOf(childKindIds);
	}

	@Override
	public List<KindHook> hooks()
	{
		return hooks;
	}

	@Override
	public void addHooksToBeginning(final List<KindHook> hooks)
	{
		final ImmutableList.Builder<KindHook> builder = ImmutableList.builder();
		if (hooks != null)
		{
			builder.addAll(hooks);
		}
		this.hooks = builder.addAll(this.hooks).build();
	}

}
