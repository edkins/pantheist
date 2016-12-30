package restless.handler.kind.backend;

import java.util.Optional;

import restless.handler.entity.model.Entity;
import restless.handler.kind.model.Kind;

public interface KindValidation
{
	boolean validateEntityAgainstKind(Entity entity, Kind kind);

	Optional<Entity> discoverKind(Entity entity);

	boolean validateEntityAgainstStoredKind(Entity entity);
}
