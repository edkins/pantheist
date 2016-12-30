package restless.handler.kind.backend;

import restless.common.util.AntiIterator;
import restless.common.util.Possible;
import restless.handler.kind.model.Kind;

public interface KindStore
{
	Possible<Void> putKind(String kindId, Kind kind);

	Possible<Kind> getKind(String kindId);

	/**
	 * Returns all kinds that are discoverable
	 */
	AntiIterator<Kind> discoverKinds();

	AntiIterator<String> listKindIds();
}
