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
	private final Map<String, TypeInfo> itemProperties;

	private KindPropertyImpl(
			@JsonProperty("type") final PropertyType type,
			@JsonProperty("isIdentifier") final boolean isIdentifier,
			@Nullable @JsonProperty("itemProperties") final Map<String, TypeInfo> itemProperties)
	{
		this.type = checkNotNull(type);
		this.isIdentifier = isIdentifier;
		this.itemProperties = itemProperties;
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
	public Map<String, TypeInfo> itemProperties()
	{
		return itemProperties;
	}

	@Override
	public TypeInfo typeInfo(final CommonSharedModelFactory modelFactory)
	{
		return modelFactory.typeInfo(type, itemProperties);
	}

}
