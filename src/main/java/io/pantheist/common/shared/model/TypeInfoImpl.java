package io.pantheist.common.shared.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

public final class TypeInfoImpl implements TypeInfo
{
	private final PropertyType type;
	private final Map<String, TypeInfo> itemProperties;

	public static final TypeInfo STRING = new TypeInfoImpl(PropertyType.STRING, null);
	public static final TypeInfo BOOLEAN = new TypeInfoImpl(PropertyType.BOOLEAN, null);
	public static final TypeInfo STRING_ARRAY = new TypeInfoImpl(PropertyType.STRING_ARRAY, null);

	@Inject
	private TypeInfoImpl(
			@Assisted @JsonProperty("type") final PropertyType type,
			@Nullable @Assisted @JsonProperty("itemProperties") final Map<String, TypeInfo> itemProperties)
	{
		this.type = checkNotNull(type);
		this.itemProperties = itemProperties;
	}

	@Override
	public PropertyType type()
	{
		return type;
	}

	@Override
	public Map<String, TypeInfo> itemProperties()
	{
		return itemProperties;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(type.toString());
		if (itemProperties != null)
		{
			sb.append('{');
			boolean first = true;
			for (final Entry<String, TypeInfo> entry : itemProperties.entrySet())
			{
				if (!first)
				{
					sb.append(',');
				}
				first = false;
				sb.append(entry.getKey()).append(':').append(entry.getValue());
			}
			sb.append('}');
		}
		return sb.toString();
	}
}
