package io.pantheist.api.entity.backend;

import io.pantheist.api.entity.model.AddRequest;
import io.pantheist.api.entity.model.ListEntityResponse;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.util.Possible;

public interface EntityBackend
{
	Possible<Void> add(String kindId, String entityId, AddRequest req);

	Possible<Void> putEntity(String kindId, String entityId, String contentType, String text, boolean failIfExists);

	Possible<KindedMime> getEntity(String kindId, String entityId);

	ListClassifierResponse listEntityClassifiers();

	Possible<ListEntityResponse> listEntitiesWithKind(String kindId);

	Possible<Void> deleteEntity(String kindId, String entityId);
}
