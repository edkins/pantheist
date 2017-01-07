package io.pantheist.api.entity.backend;

import io.pantheist.api.entity.model.AddRequest;
import io.pantheist.common.util.Possible;

public interface EntityBackend
{
	Possible<Void> add(String kindId, String entityId, AddRequest req);
}
