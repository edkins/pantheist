package io.pantheist.handler.filekind.backend;

import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface FileKindHandler
{
	/**
	 * @return a url, or KIND_DOES_NOT_SUPPORT
	 */
	Possible<String> newInstanceOfKind(Kind kind);

	FilterableObjectStream discoverFileEntities(Kind kind);

	Possible<KindedMime> getEntity(Kind kind, String entityId);

	Possible<Void> add(Kind kind, String entityId, String addName);

	Possible<Void> putEntity(Kind kind, String entityId, String text);

	Possible<Void> deleteEntity(Kind kind, String entityId);
}
