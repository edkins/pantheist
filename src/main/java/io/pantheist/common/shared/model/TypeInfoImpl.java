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
	private final Map<String, TypeInfo> properties;
	private final TypeInfo items;

	public static final TypeInfo STRING = new TypeInfoImpl(PropertyType.STRING, null, null);
	public static final TypeInfo BOOLEAN = new TypeInfoImpl(PropertyType.BOOLEAN, null, null);

	@Inject
	private TypeInfoImpl(
			@Assisted @JsonProperty("type") final PropertyType type,
			@Nullable @Assisted("items") @JsonProperty("items") final TypeInfo items,
			@Nullable @Assisted @JsonProperty("properties") final Map<String, TypeInfo> properties)
	{
		this.type = checkNotNull(type);
		this.items = items;
		this.properties = properties;
	}

	@Override
	public PropertyType type()
	{
		return type;
	}

	@Override
	public Map<String, TypeInfo> properties()
	{
		return properties;
	}

	@Override
	public TypeInfo items()
	{
		return items;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		if (items != null)
		{
			sb.append('[');
			sb.append(items);
			sb.append(']');
		}
		else if (properties != null)
		{
			sb.append('{');
			boolean first = true;
			for (final Entry<String, TypeInfo> entry : properties.entrySet())
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
		else
		{
			sb.append(type.toString());
		}
		return sb.toString();
	}
}
