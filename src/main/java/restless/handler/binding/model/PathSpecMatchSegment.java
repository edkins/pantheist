package restless.handler.binding.model;

import java.util.List;

public interface PathSpecMatchSegment
{
	PathSpecSegment matcher();

	List<PathSpecSegment> matched();
}
