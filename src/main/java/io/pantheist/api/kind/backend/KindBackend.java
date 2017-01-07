package io.pantheist.api.kind.backend;

import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface KindBackend
{
	Possible<Kinded<Kind>> getKind(String kindId);

	Possible<Void> putKindData(String kindId, Kind kind, boolean failIfExists);

	Possible<String> postKind(Kind kind);

	ListKindResponse listKinds();

	Possible<String> newInstanceOfKind(String kindId);
}
