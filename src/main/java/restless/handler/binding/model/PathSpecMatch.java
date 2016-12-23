package restless.handler.binding.model;

import java.util.List;

/**
 * Returned when one path spec matches another. It contains information on what
 * happened.
 *
 * Note that "$" should really be "*" in most of these examples, but I can't
 * write that inside a comment.
 */
public interface PathSpecMatch
{
	/**
	 * These correspond to the path segments in the matched path spec. So if
	 * foo/$$/bar matches foo/x/y/bar, then there will be three segments here
	 * corresponding to foo, $$ and bar.
	 */
	List<PathSpecMatchSegment> segments();

	/**
	 * Returns all the non-literal matched path segments concatenated together.
	 *
	 * Matcher Matched nonLiteralChunk foo/$$/bar foo/x/y/bar x/y
	 *
	 * foo/$/y/$ foo/x/y/bar x/bar
	 *
	 * $$/y/$ foo/x/y/bar foo/x/bar
	 *
	 * This will throw an exception if there are multiple segments in the
	 * matcher that match a nondeterministic number of things. This is to avoid
	 * collisions.
	 */
	List<PathSpecSegment> nonLiteralChunk();
}
