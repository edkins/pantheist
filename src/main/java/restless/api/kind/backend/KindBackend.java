package restless.api.kind.backend;

import restless.api.entity.model.ListEntityResponse;
import restless.api.kind.model.ApiKind;
import restless.api.kind.model.ListKindResponse;
import restless.common.util.Possible;

public interface KindBackend
{
	Possible<ApiKind> getKind(String kindId);

	Possible<Void> putKind(String kindId, ApiKind kind);

	Possible<ListEntityResponse> listEntitiesWithKind(String kindId);

	ListKindResponse listKinds();
}
