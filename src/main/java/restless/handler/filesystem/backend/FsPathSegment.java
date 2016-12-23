package restless.handler.filesystem.backend;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FsPathSegmentImpl.class)
interface FsPathSegment
{
	@Override
	@JsonValue
	String toString();
}
