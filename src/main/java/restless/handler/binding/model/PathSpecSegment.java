package restless.handler.binding.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = PathSpecSegmentImpl.class)
public interface PathSpecSegment
{
	@JsonProperty("type")
	PathSpecSegmentType type();

	@JsonProperty("value")
	String value();

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

	String nameHint();
}
