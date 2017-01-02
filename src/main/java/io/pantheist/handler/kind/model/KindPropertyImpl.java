package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;

final class KindPropertyImpl implements KindProperty
{
	private final PropertyType type;
	private final boolean isIdentifier;
	private final TypeInfo items;

	private KindPropertyImpl(
			@JsonProperty("type") final PropertyType type,
			@JsonProperty("isIdentifier") final boolean isIdentifier,
			@Nullable @JsonProperty("items") final TypeInfo items)
	{
		this.type = checkNotNull(type);
		this.isIdentifier = isIdentifier;
		this.items = items;
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

	@Override
	public TypeInfo items()
	{
		return items;
	}

}
