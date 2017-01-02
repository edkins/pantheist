package io.pantheist.common.shared.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class TypeInfoImpl implements TypeInfo
{
	private final PropertyType type;

	@Inject
	private TypeInfoImpl(@Assisted @JsonProperty("type") final PropertyType type)
	{
		if (type == PropertyType.ARRAY)
		{
			throw new IllegalArgumentException("array type currently not allowed here");
		}
		this.type = checkNotNull(type);
	}

	@Override
	public PropertyType type()
	{
		return type;
	}

}
