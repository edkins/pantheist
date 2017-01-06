package io.pantheist.handler.filekind.backend;

import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface FileKindHandler
{
	/**
	 * @return a url
	 */
	String newInstanceOfKind(Kind kind);

	FilterableObjectStream discoverFileEntities(Kind kind);

	Possible<KindedMime> getEntity(Kind kind, String entityId);
}
