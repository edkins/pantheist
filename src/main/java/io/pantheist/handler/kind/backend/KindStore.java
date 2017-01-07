package io.pantheist.handler.kind.backend;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.sql.model.SqlProperty;

/**
 * @deprecated these methods don't really make sense being here any more
 */
@Deprecated
public interface KindStore
{
	void registerKindsInSql();

	/**
	 * Lists sql properties belonging to this kind.
	 *
	 * Returns an empty sequence if kindId does not exist, or is not one of
	 * the base kinds defining sql properties. (Subkinds are no good here).
	 */
	@Deprecated
	AntiIterator<SqlProperty> listSqlPropertiesOfKind(String kindId);
}
