package restless.handler.entity.backend;

import java.util.Optional;

import restless.handler.entity.model.Entity;

public interface EntityStore
{
	void putEntity(String entityId, Entity entity);

	Optional<Entity> getEntity(String entityId);
}
