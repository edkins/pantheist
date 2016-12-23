package restless.handler.binding.model;

import java.util.List;

import javax.inject.Named;

public interface BindingModelFactory
{
	PathSpec pathSpec(List<PathSpecSegment> segments);

	@Named("literal")
	PathSpecSegment literal(String value);

	@Named("star")
	PathSpecSegment star();

	@Named("multi")
	PathSpecSegment multi();
}
