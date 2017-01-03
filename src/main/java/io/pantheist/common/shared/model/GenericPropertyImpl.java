package io.pantheist.common.shared.model;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyImpl implements GenericProperty
{
	private final String name;
	private final TypeInfo typeInfo;

	@Inject
	private GenericPropertyImpl(
			@Assisted("name") final String name,
			@Assisted final TypeInfo typeInfo)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.typeInfo = typeInfo;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public TypeInfo typeInfo()
	{
		return typeInfo;
	}

	@Override
	public String toString()
	{
		return name + ":" + typeInfo;
	}
}
