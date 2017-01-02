package io.pantheist.handler.kind.backend;

import java.util.Optional;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface KindStore
{
	Possible<Void> putKind(String kindId, Kind kind);

	/**
	 * Returns the kind with the given id, or empty if missing.
	 */
	Optional<Kind> getKind(String kindId);

	AntiIterator<Kind> listAllKinds();

	AntiIterator<Kind> listChildKinds(String parentId);

	void registerKindsInSql();
}
