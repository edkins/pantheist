package io.pantheist.api.kind.backend;

import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

public interface KindBackend
{
	Possible<ApiKind> getKindInfo(String kindId);

	Possible<Kind> getKindData(String kindId);

	Possible<Void> putKindData(String kindId, Kind kind, boolean failIfExists);

	Possible<String> postKind(Kind kind);

	Possible<ListEntityResponse> listEntitiesWithKind(String kindId);

	ListKindResponse listKinds();
}
