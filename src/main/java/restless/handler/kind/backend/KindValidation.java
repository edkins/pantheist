package restless.handler.kind.backend;

import java.util.Optional;

import restless.common.util.AntiIterator;
import restless.handler.entity.model.Entity;
import restless.handler.java.model.JavaFileId;
import restless.handler.kind.model.Kind;

public interface KindValidation
{
	boolean validateEntityAgainstKind(Entity entity, Kind kind);

	Optional<Entity> discoverJavaKind(JavaFileId javaFileId);

	boolean validateEntityAgainstStoredKind(Entity entity);

	AntiIterator<Entity> discoverEntitiesWithKind(Kind kind);
}
