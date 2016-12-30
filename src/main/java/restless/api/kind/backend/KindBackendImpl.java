package restless.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.api.entity.model.ApiEntityModelFactory;
import restless.api.entity.model.ListEntityItem;
import restless.api.entity.model.ListEntityResponse;
import restless.api.kind.model.ApiKind;
import restless.api.kind.model.ApiKindModelFactory;
import restless.api.kind.model.ListKindResponse;
import restless.common.util.FailureReason;
import restless.common.util.OtherPreconditions;
import restless.common.util.Possible;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.Entity;
import restless.handler.kind.backend.KindStore;
import restless.handler.kind.backend.KindValidation;
import restless.handler.kind.model.Kind;
import restless.handler.kind.model.KindModelFactory;
import restless.handler.uri.UrlTranslation;

final class KindBackendImpl implements KindBackend
{
	private final KindStore kindStore;
	private final KindModelFactory kindFactory;
	private final KindValidation kindValidation;
	private final ApiKindModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final EntityStore entityStore;
	private final ApiEntityModelFactory entityFactory;

	@Inject
	private KindBackendImpl(
			final KindStore kindStore,
			final KindModelFactory kindFactory,
			final KindValidation kindValidation,
			final ApiKindModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final EntityStore entityStore,
			final ApiEntityModelFactory entityFactory)
	{
		this.kindStore = checkNotNull(kindStore);
		this.kindFactory = checkNotNull(kindFactory);
		this.kindValidation = checkNotNull(kindValidation);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.entityStore = checkNotNull(entityStore);
		this.entityFactory = checkNotNull(entityFactory);
	}

	private ApiKind toApiKind(final Kind k)
	{
		return modelFactory.kind(urlTranslation.listKindClassifiers(k.kindId()),
				k.kindId(), k.level(), k.discoverable(), k.java());
	}

	@Override
	public Possible<ApiKind> getKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindStore.getKind(kindId).map(this::toApiKind);
	}

	private Kind supplyKindId(final String kindId, final ApiKind kind)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindFactory.kind(kindId, kind.level(), kind.discoverable(), kind.java());
	}

	@Override
	public Possible<Void> putKind(final String kindId, final ApiKind kind)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		checkNotNull(kind);
		if (kind.kindId() != null && !kind.kindId().equals(kindId))
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		return kindStore.putKind(kindId, supplyKindId(kindId, kind));
	}

	private ListEntityItem toListEntityItem(final Entity entity)
	{
		return entityFactory.listEntityItem(
				urlTranslation.entityToUrl(entity.entityId()),
				entity.entityId(),
				entity.discovered());
	}

	@Override
	public Possible<ListEntityResponse> listEntitiesWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindStore.getKind(kindId).map(kind -> {
			if (kind.discoverable())
			{
				return kindValidation.discoverEntitiesWithKind(kind)
						.map(this::toListEntityItem)
						.wrap(entityFactory::listEntityResponse);
			}
			else
			{
				return listStoredEntitiesWithKind(kindId);
			}
		});
	}

	private ListEntityResponse listStoredEntitiesWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return entityStore.listEntities()
				.filter(e -> kindId.equals(e.kindId()))
				.map(this::toListEntityItem)
				.wrap(entityFactory::listEntityResponse);
	}

	@Override
	public ListKindResponse listKinds()
	{
		return kindStore.listKindIds()
				.map(urlTranslation::kindToUrl)
				.map(modelFactory::listKindItem)
				.wrap(modelFactory::listKindResponse);
	}
}
