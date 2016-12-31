package io.pantheist.handler.entity.backend;

import java.util.Optional;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.entity.model.Entity;

public interface EntityStore
{
	void putEntity(String entityId, Entity entity);

	Optional<Entity> getEntity(String entityId);

	AntiIterator<Entity> listEntities();
}
