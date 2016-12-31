package io.pantheist.handler.kind.backend;

import java.util.Optional;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.entity.model.Entity;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Kind;

public interface KindValidation
{
	boolean validateEntityAgainstKind(Entity entity, Kind kind);

	Optional<Entity> discoverJavaKind(JavaFileId javaFileId);

	boolean validateEntityAgainstStoredKind(Entity entity);

	AntiIterator<Entity> discoverEntitiesWithKind(Kind kind);
}
