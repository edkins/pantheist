package restless.client.api;

import restless.handler.kind.model.Kind;

public interface ManagementPathKind
{
	void putJsonResource(String resourcePath);

	Kind getKind();
}
