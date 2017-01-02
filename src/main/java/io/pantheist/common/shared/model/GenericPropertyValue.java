package io.pantheist.common.shared.model;

public interface GenericPropertyValue
{
	String name();

	PropertyType type();

	/**
	 * @throws IllegalStateException if type is not boolean
	 */
	boolean booleanValue();

	/**
	 * @throws IllegalStateException if type is not string
	 */
	String stringValue();
}
