package io.pantheist.handler.kind.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonValue;

import io.pantheist.common.util.OtherPreconditions;

/**
 * Anybody know the correct name for this? What do the concepts of a class, interface and enum
 * (and possibly annotation) have in common?
 */
public enum JavaKind
{
	CLASS("class"),
	INTERFACE("interface"),
	ENUM("enum"),
	TYPE("type");

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

	public boolean encompasses(final Optional<JavaKind> optOther)
	{
		if (!optOther.isPresent())
		{
			return true;
		}
		final JavaKind other = optOther.get();
		if (other == TYPE)
		{
			throw new IllegalArgumentException("other may not be TYPE here");
		}

		switch (this) {
		case TYPE:
			return other == CLASS || other == INTERFACE || other == ENUM;
		case CLASS:
		case INTERFACE:
		case ENUM:
			return this == other;
		default:
			throw new IllegalStateException("This enum state should not be possible");
		}
	}
}
