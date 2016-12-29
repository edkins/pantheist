package restless.handler.kind.backend;

import restless.common.util.Possible;
import restless.handler.kind.model.Kind;

public interface KindStore
{
	Possible<Void> putKind(String kindId, Kind kind);

	Possible<Kind> getKind(String kindId);
}
