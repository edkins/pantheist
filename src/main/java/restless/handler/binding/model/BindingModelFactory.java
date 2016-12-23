package restless.handler.binding.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface BindingModelFactory
{
	PathSpec pathSpec(List<PathSpecSegment> segments);

	PathSpecSegment pathSpecSegment(PathSpecSegmentType type, @Assisted("value") String value);
}
