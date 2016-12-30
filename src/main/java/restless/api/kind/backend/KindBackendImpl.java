package restless.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.common.util.FailureReason;
import restless.common.util.OtherPreconditions;
import restless.common.util.Possible;
import restless.handler.kind.backend.KindStore;
import restless.handler.kind.model.Kind;
import restless.handler.kind.model.KindModelFactory;

final class KindBackendImpl implements KindBackend
{
	private final KindStore kindStore;
	private final KindModelFactory kindFactory;

	@Inject
	private KindBackendImpl(
			final KindStore kindStore,
			final KindModelFactory kindFactory)
	{
		this.kindStore = checkNotNull(kindStore);
		this.kindFactory = checkNotNull(kindFactory);
	}

	@Override
	public Possible<Kind> getKind(final String kindId)
	{
		return kindStore.getKind(kindId);
	}

	private Kind supplyKindId(final String kindId, final Kind kind)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindFactory.kind(kindId, kind.level(), kind.discoverable(), kind.java());
	}

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind)
	{
		if (kind.kindId() != null && !kind.kindId().equals(kindId))
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		return kindStore.putKind(kindId, supplyKindId(kindId, kind));
	}
}
