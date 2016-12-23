package restless.handler.binding.backend;

import restless.handler.binding.model.HandlerType;
import restless.handler.binding.model.PathSpec;

public interface BindingStore
{
	void bind(PathSpec pathSpec, HandlerType handlerType);

	ManagementFunctions lookup(PathSpec pathSpec);

	void initialize();

	void stop();
}
