package restless.api.management.backend;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;

import restless.api.management.model.CreateConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;
import restless.handler.binding.model.ConfigId;
import restless.handler.binding.model.Handler;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.Schema;

public interface ManagementBackend
{
	URI createConfig(CreateConfigRequest request);

	PathSpec literalPath(String path);

	PossibleEmpty putConfig(ConfigId path, Handler handler);

	PossibleEmpty putData(PathSpec path, String data);

	PossibleData getData(PathSpec path);

	PossibleEmpty putJsonSchema(ConfigId pathSpec, JsonNode schema);

	Schema getSchema(ConfigId pathSpec);

	PossibleEmpty putJerseyFile(ConfigId pathSpec, String code);

	PossibleData getJerseyFile(ConfigId pathSpec);
}
