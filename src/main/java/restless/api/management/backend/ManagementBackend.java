package restless.api.management.backend;

import com.fasterxml.jackson.databind.JsonNode;

import restless.api.management.model.ConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.Schema;

public interface ManagementBackend
{
	PathSpec pathSpec(String path);

	void putConfig(PathSpec path, ConfigRequest config);

	void putData(PathSpec path, String data);

	PossibleData getData(PathSpec path);

	void putJsonSchema(PathSpec pathSpec, JsonNode schema);

	Schema getSchema(PathSpec pathSpec);
}
