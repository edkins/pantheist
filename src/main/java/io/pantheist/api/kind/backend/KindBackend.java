package io.pantheist.api.kind.backend;

import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface KindBackend
{
	Possible<String> postKind(Kind kind);

	Possible<String> newInstanceOfKind(String kindId);
}
