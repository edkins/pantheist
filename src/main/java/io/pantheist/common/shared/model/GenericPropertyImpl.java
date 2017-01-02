package io.pantheist.common.shared.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyImpl implements GenericProperty
{
	private final String name;
	private final PropertyType type;
	private final boolean isIdentifier;

	private GenericPropertyImpl(
			@JsonProperty("name") final String name,
			@JsonProperty("type") final PropertyType type,
			@JsonProperty("isIdentifier") final boolean isIdentifier)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.type = checkNotNull(type);
		this.isIdentifier = isIdentifier;
	}

	@Override
	public String name()
	{
		return name;
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
