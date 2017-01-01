package io.pantheist.handler.kind.backend;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface KindStore
{
	Possible<Void> putKind(String kindId, Kind kind);

	Possible<Kind> getKind(String kindId);

	/**
	 * Returns all kinds that are discoverable
	 */
	AntiIterator<Kind> discoverKinds();

	AntiIterator<Kind> listAllKinds();
}
