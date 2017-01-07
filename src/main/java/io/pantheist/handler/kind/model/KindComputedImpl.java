package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

final class KindComputedImpl implements KindComputed
{
	private String mimeType;
	private KindHandler handler;
	private boolean isEntityKind;

	private KindComputedImpl(
			@Nullable @JsonProperty("mimeType") final String mimeType,
			@Nullable @JsonProperty("handler") final KindHandler handler,
			@Deprecated @JsonProperty("isEntityKind") final boolean isEntityKind)
	{
		this.mimeType = mimeType;
		this.handler = handler;
		this.isEntityKind = isEntityKind;
	}

	/**
	 * Arbitrary because someone will call clear() later.
	 */
	static KindComputed arbitrary()
	{
		return new KindComputedImpl(null, null, false);
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
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("mimeType", mimeType)
				.add("handler", handler)
				.add("isEntityKind", isEntityKind)
				.toString();
	}

}
