package restless.api.management.backend;

import restless.api.management.model.ConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.model.PathSpec;

public interface ManagementBackend
{
	PathSpec pathSpec(String path);

	void putConfig(PathSpec path, ConfigRequest config);

	void putData(PathSpec path, String data);

	PossibleData getData(PathSpec path);
}
