package restless.handler.entity.backend;

import restless.common.util.Possible;
import restless.handler.entity.model.Entity;

public interface EntityStore
{
	Possible<Void> putEntity(String entityId, Entity entity);

	Possible<Entity> getEntity(String entityId);
}
