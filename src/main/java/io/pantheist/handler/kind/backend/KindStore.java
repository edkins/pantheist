package io.pantheist.handler.kind.backend;

import java.util.Optional;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.sql.model.SqlProperty;

public interface KindStore
{
	Possible<Void> putKind(String kindId, Kind kind, boolean failIfExists);

	/**
	 * Returns the kind with the given id, or empty if missing.
	 */
	Optional<Kind> getKind(String kindId);

	AntiIterator<Kind> listAllKinds();

	AntiIterator<Kind> listChildKinds(String parentId);

	void registerKindsInSql();

	/**
	 * Lists sql properties belonging to this kind.
	 *
	 * Returns an empty sequence if kindId does not exist, or is not one of
	 * the base kinds defining sql properties. (Subkinds are no good here).
	 */
	AntiIterator<SqlProperty> listSqlPropertiesOfKind(String kindId);

	boolean derivesFrom(Kind kind, String ancestorKindId);
}
