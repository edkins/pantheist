package restless.api.management.backend;

import com.fasterxml.jackson.databind.JsonNode;

import restless.api.management.model.ConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;
import restless.handler.binding.model.ConfigId;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.Schema;

public interface ManagementBackend
{
	@Deprecated
	ConfigId pathSpec(String path);

	PathSpec literalPath(String path);

	PossibleEmpty putConfig(ConfigId path, ConfigRequest config);

	PossibleEmpty putData(PathSpec path, String data);

	PossibleData getData(PathSpec path);

	PossibleEmpty putJsonSchema(ConfigId pathSpec, JsonNode schema);

	Schema getSchema(ConfigId pathSpec);

	PossibleEmpty putJerseyFile(ConfigId pathSpec, String code);

	PossibleData getJerseyFile(ConfigId pathSpec);
}
