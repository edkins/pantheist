package io.pantheist.api.entity.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.inject.Inject;

import io.pantheist.api.entity.model.AddRequest;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.model.Kind;

final class EntityBackendImpl implements EntityBackend
{
	private final KindStore kindStore;
	private final FileKindHandler fileKindHandler;

	@Inject
	private EntityBackendImpl(final KindStore kindStore, final FileKindHandler fileKindHandler)
	{
		this.kindStore = checkNotNull(kindStore);
		this.fileKindHandler = checkNotNull(fileKindHandler);
	}

	@Override
	public Possible<Void> add(final String kindId, final String entityId, final AddRequest req)
	{
		final Optional<Kind> kind = kindStore.getKind(kindId);
		if (kind.isPresent())
		{
			if (kind.get().hasParent("file"))
			{
				return fileKindHandler.add(kind.get(), entityId, req.addName());
			}
			else
			{
				return FailureReason.KIND_DOES_NOT_SUPPORT.happened();
			}
		}
		else
		{
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

}
