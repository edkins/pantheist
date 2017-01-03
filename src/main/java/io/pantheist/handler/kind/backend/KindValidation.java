package io.pantheist.handler.kind.backend;

import io.pantheist.common.util.FilterableObjectStream;

public interface KindValidation
{
	/**
	 * Objects returned will include a kindId, which may be a subkind
	 * of the one requested here.
	 *
	 * They'll also include an entityId.
	 */
	FilterableObjectStream objectsWithKind(String kindId);
}
