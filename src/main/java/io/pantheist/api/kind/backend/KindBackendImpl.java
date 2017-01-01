package io.pantheist.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ApiKindModelFactory;
import io.pantheist.api.kind.model.ListEntityItem;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.api.kind.model.ListKindItem;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.backend.KindValidation;
import io.pantheist.handler.kind.model.Entity;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindModelFactory;

final class KindBackendImpl implements KindBackend
{
	private final KindStore kindStore;
	private final KindModelFactory kindFactory;
	private final KindValidation kindValidation;
	private final ApiKindModelFactory modelFactory;
	private final UrlTranslation urlTranslation;

	@Inject
	private KindBackendImpl(
			final KindStore kindStore,
			final KindModelFactory kindFactory,
			final KindValidation kindValidation,
			final ApiKindModelFactory modelFactory,
			final UrlTranslation urlTranslation)
	{
		this.kindStore = checkNotNull(kindStore);
		this.kindFactory = checkNotNull(kindFactory);
		this.kindValidation = checkNotNull(kindValidation);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
	}

	private ApiKind toApiKind(final Kind k)
	{
		return modelFactory.kind(
				urlTranslation.listKindClassifiers(k.kindId()),
				urlTranslation.listKindReplaceAction(k.kindId()),
				k.kindId(), k.schema(), k.partOfSystem(), k.instancePresentation());
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
		return kindFactory.kind(kindId, kind.schema(), kind.partOfSystem(), kind.instancePresentation());
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
		return modelFactory.listEntityItem(
				urlTranslation.entityToUrl(entity.entityId()),
				entity.entityId(),
				entity.discovered(),
				kindUrlForEntity(entity));
	}

	private String kindUrlForEntity(final Entity entity)
	{
		final String kindId = Optional.ofNullable(entity.kindId()).orElse("unknown");
		return urlTranslation.kindToUrl(kindId);
	}

	@Override
	public Possible<ListEntityResponse> listEntitiesWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindStore.getKind(kindId).map(kind -> {
			return kindValidation.discoverEntitiesWithKind(kind)
					.map(this::toListEntityItem)
					.wrap(modelFactory::listEntityResponse);
		});
	}

	private ListKindItem toListKindItem(final Kind kind)
	{
		final String url = urlTranslation.kindToUrl(kind.kindId());
		final String kindUrl = urlTranslation.kindToUrl("kind"); // this is the meta-kind
		return modelFactory.listKindItem(url, kindUrl, kind.instancePresentation());
	}

	@Override
	public ListKindResponse listKinds()
	{
		final List<ListKindItem> list = kindStore.listAllKinds()
				.map(this::toListKindItem)
				.toSortedList((k1, k2) -> k1.url().compareTo(k2.url()));
		return modelFactory.listKindResponse(list, urlTranslation.kindCreateAction());
	}
}
