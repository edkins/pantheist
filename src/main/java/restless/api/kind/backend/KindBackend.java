package restless.api.kind.backend;

import restless.common.util.Possible;
import restless.handler.kind.model.Kind;

public interface KindBackend
{
	Possible<Kind> getKind(String kindId);

	Possible<Void> putKind(String kindId, Kind kind);
}
