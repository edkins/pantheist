package io.pantheist.handler.kind.backend;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Entity;
import io.pantheist.handler.kind.model.Kind;

public interface KindValidation
{
	boolean validateEntityAgainstKind(Entity entity, Kind kind);

	Entity discoverJavaKind(JavaFileId javaFileId);

	boolean validateEntityAgainstStoredKind(Entity entity);

	AntiIterator<Entity> discoverEntitiesWithKind(Kind kind);
}
