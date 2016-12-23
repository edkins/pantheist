package restless.handler.filesystem.backend;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FsBoundPathsImpl.class)
public interface FsBoundPaths
{
	@JsonProperty("bound")
	boolean bound();

	@JsonProperty("children")
	Map<FsPathSegment, FsBoundPaths> children();

	/**
	 * Returns a new object which has the additional (relative) path bound.
	 */
	@JsonIgnore
	FsBoundPaths withBoundPath(FsPath path);

	@JsonIgnore
	boolean isEmpty();

	/**
	 * Return what's going on at the given segment. This may return an empty
	 * FsBoundPaths.
	 *
	 * @throws IllegalStateException
	 *             if bound.
	 */
	@JsonIgnore
	FsBoundPaths at(FsPathSegment seg);

	/**
	 * Return whether the given path can be added to this bundle without
	 * conflict.
	 */
	@JsonIgnore
	boolean canAccommodate(FsPath path);

	/**
	 * Return a copy of this without the given path.
	 *
	 * @throws IllegalStateException
	 *             if the path was not explicitly bound.
	 */
	FsBoundPaths withoutPath(FsPath path);
}
