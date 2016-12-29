package restless.client.api;

import javax.annotation.Nullable;

import restless.api.management.model.ApiEntity;

public interface ManagementPathEntity
{
	void putEntity(@Nullable String jsonSchemaUrl, @Nullable String javaUrl);

	ApiEntity getEntity();
}
