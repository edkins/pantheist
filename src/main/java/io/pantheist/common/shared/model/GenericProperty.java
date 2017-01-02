package io.pantheist.common.shared.model;

import javax.annotation.Nullable;

public interface GenericProperty
{
	String name();

	PropertyType type();

	@Nullable
	TypeInfo items();
}
