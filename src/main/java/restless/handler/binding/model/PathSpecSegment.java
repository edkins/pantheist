package restless.handler.binding.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PathSpecSegmentLiteralImpl.class, name = "literal"),
		@JsonSubTypes.Type(value = PathSpecSegmentMultiImpl.class, name = "multi") })
public interface PathSpecSegment
{
	PathSpecSegmentType type();

	@JsonIgnore
	boolean literal();

	@JsonIgnore
	boolean matches(PathSpecSegment segment);

	/**
	 * @return whether this segment matches a fixed number of things. Literal
	 *         segments and "*" both do.
	 *
	 *         "**" does not.
	 */
	@JsonIgnore
	boolean fixedNumber();

	@JsonIgnore
	String nameHint();

	/**
	 * Return the value of this segment, but only if it is a literal string. No
	 * escaping is performed here.
	 */
	@JsonIgnore
	String literalValue();

	/**
	 * Return the value of this segment, but only if it is a literal string.
	 * Unusual characters will be escaped.
	 */
	@JsonIgnore
	String escapedLiteralValue();
}
