package restless.handler.binding.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = PathSpecImpl.class)
public interface PathSpec
{
	@JsonProperty("segments")
	List<PathSpecSegment> segments();

	/**
	 * Return whether this path spec matches a given literal path.
	 *
	 * (path can't contain non-literal segments such as "*" or "**").
	 *
	 * If it does then return a PathSpecMatch object giving some information on
	 * what matched what. Otherwise return empty.
	 */
	@JsonIgnore
	Optional<PathSpecMatch> match(PathSpec path);

	/**
	 * Something which looks vaguely like this PathSpec. Not guaranteed to be
	 * unique or anything.
	 */
	@JsonIgnore
	String nameHint();
}
