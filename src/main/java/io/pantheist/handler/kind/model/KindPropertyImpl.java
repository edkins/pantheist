package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;

final class KindPropertyImpl implements KindProperty
{
	private final PropertyType type;
	private final boolean isIdentifier;
	private final TypeInfo items;
	private final Map<String, TypeInfo> properties;

	private KindPropertyImpl(
			@JsonProperty("type") final PropertyType type,
			@JsonProperty("isIdentifier") final boolean isIdentifier,
			@Nullable @JsonProperty("items") final TypeInfo items,
			@Nullable @JsonProperty("itemProperties") final Map<String, TypeInfo> properties)
	{
		this.type = checkNotNull(type);
		this.isIdentifier = isIdentifier;
		this.items = items;
		this.properties = properties;
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
	public Map<String, TypeInfo> properties()
	{
		return properties;
	}

	@Override
	public TypeInfo typeInfo(final CommonSharedModelFactory modelFactory)
	{
		return modelFactory.typeInfo(type, items, properties);
	}

	@Override
	public TypeInfo items()
	{
		return items;
	}

}
