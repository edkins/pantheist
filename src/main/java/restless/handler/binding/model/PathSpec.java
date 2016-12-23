package restless.handler.binding.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = PathSpecImpl.class)
public interface PathSpec
{
	@JsonProperty("segments")
	List<PathSpecSegment> segments();

	@JsonIgnore
	boolean contains(PathSpec other);

	@JsonIgnore
	PathSpec relativeTo(PathSpec other);
}
