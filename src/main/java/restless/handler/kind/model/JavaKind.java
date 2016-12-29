package restless.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonValue;

import restless.common.util.OtherPreconditions;

/**
 * Anybody know the correct name for this? What do the concepts of a class, interface and enum
 * (and possibly annotation) have in common?
 */
public enum JavaKind
{
	CLASS("class"),
	INTERFACE("interface"),
	ENUM("enum");

	private final String value;

	private JavaKind(final String value)
	{
		this.value = OtherPreconditions.checkNotNullOrEmpty(value);
	}

	@JsonValue
	@Override
	public String toString()
	{
		return value;
	}
}
