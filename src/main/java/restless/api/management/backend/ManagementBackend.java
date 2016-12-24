package restless.api.management.backend;

import com.fasterxml.jackson.databind.JsonNode;

import restless.api.management.model.ConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.Schema;

public interface ManagementBackend
{
	PathSpec pathSpec(String path);

	PossibleEmpty putConfig(PathSpec path, ConfigRequest config);

	PossibleEmpty putData(PathSpec path, String data);

	PossibleData getData(PathSpec path);

	PossibleEmpty putJsonSchema(PathSpec pathSpec, JsonNode schema);

	Schema getSchema(PathSpec pathSpec);

	PossibleEmpty putJerseyFile(PathSpec pathSpec, String code);

	PossibleData getJerseyFile(PathSpec pathSpec);
}
