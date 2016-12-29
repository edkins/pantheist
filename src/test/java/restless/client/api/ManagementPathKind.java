package restless.client.api;

import restless.handler.kind.model.Kind;

public interface ManagementPathKind
{
	void putJsonResource(String resourcePath);

	ResponseType putJsonResourceResponseType(String resourcePath);

	Kind getKind();

	String url();
}
