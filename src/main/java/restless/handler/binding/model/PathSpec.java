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

	/**
	 * Classify this path spec according to common patterns.
	 *
	 * EXACT: all segments are literal. This identifies a single resource only.
	 *
	 * PREFIX: the path matches everything under a given directory, e.g. a/b/$$
	 *
	 * PREFIX_STAR: the prefix matches things in a given directory, one level
	 * deep e.g. a/b/$
	 *
	 * OTHER: a catch-all for anything that doesn't fit one of those categories.
	 */
	@JsonIgnore
	PathSpecClassification classify();

	/**
	 * Return a PathSpec with the last segment removed. You specify what that
	 * segment is to avoid mistakes, since different types of segment serve very
	 * different purposes.
	 *
	 * @throws IllegalArgumentException
	 *             If the segment is wrong or the path is empty
	 */
	@JsonIgnore
	PathSpec minus(PathSpecSegment segment);

	/**
	 * Returns this path as a string, with leading and trailing slashes. Fails
	 * if any segment is not literal.
	 *
	 * Unusual characters will be escaped.
	 */
	@JsonIgnore
	String literalString();

	/**
	 * @return an empty binding associated with this path spec.
	 */
	@JsonIgnore
	Binding emptyBinding();
}
