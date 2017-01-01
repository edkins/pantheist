package io.pantheist.api.kind.backend;

import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.util.Possible;

public interface KindBackend
{
	Possible<ApiKind> getKind(String kindId);

	Possible<Void> putKind(String kindId, ApiKind kind);

	Possible<ListEntityResponse> listEntitiesWithKind(String kindId);

	ListKindResponse listKinds();
}
