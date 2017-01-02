package io.pantheist.common.shared.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyImpl implements GenericProperty
{
	private final String name;
	private final PropertyType type;

	@Inject
	private GenericPropertyImpl(@Assisted("name") final String name, @Assisted final PropertyType type)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.type = checkNotNull(type);
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

}
