package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.shared.model.PropertyType;

final class KindPropertyImpl implements KindProperty
{
	private final PropertyType type;
	private final boolean isIdentifier;

	private KindPropertyImpl(
			@JsonProperty("type") final PropertyType type,
			@JsonProperty("isIdentifier") final boolean isIdentifier)
	{
		this.type = checkNotNull(type);
		this.isIdentifier = isIdentifier;
	}

	@Override
	public PropertyType type()
	{
		return type;
	}

	@Override
	public boolean isIdentifier()
	{
		return isIdentifier;
	}

}
