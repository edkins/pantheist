package io.pantheist.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ApiKindModelFactory;
import io.pantheist.api.kind.model.ListEntityItem;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.backend.KindValidation;
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
		final String kindId = k.kindId();
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		final String url = urlTranslation.kindToUrl(kindId);
		final String kindUrl = urlTranslation.kindToUrl("kind"); // this is the meta-kind
		return modelFactory.kind(
				url,
				kindUrl,
				urlTranslation.listKindClassifiers(kindId),
				urlTranslation.kindDataAction(kindId),
				kindId, k.schema(), k.partOfSystem(), k.presentation(), k.createAction(),
				k.deleteAction());
	}

	@Override
	public Possible<ApiKind> getKindInfo(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindStore.getKind(kindId)
				.map(k -> View.ok(toApiKind(k)))
				.orElse(FailureReason.DOES_NOT_EXIST.happened());
	}

	@Override
	public Possible<Kind> getKindData(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindStore.getKind(kindId)
				.map(View::ok)
				.orElse(FailureReason.DOES_NOT_EXIST.happened());
	}

	private Kind supplyKindId(final String kindId, final Kind kind)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindFactory.kind(kindId, kind.schema(), kind.partOfSystem(), kind.presentation(),
				kind.createAction(), kind.deleteAction());
	}

	@Override
	public Possible<Void> putKindData(final String kindId, final Kind kind, final boolean failIfExists)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		checkNotNull(kind);
		if (kind.kindId() != null && !kind.kindId().equals(kindId))
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		return kindStore.putKind(kindId, supplyKindId(kindId, kind), failIfExists);
	}

	private ListEntityItem toListEntityItem(final ObjectNode entity)
	{
		final String kindId = entity.get("kindId").textValue();
		final String entityId = entity.get("entityId").textValue();
		return modelFactory.listEntityItem(
				urlTranslation.entityToUrl(kindId, entityId),
				entityId,
				urlTranslation.kindToUrl(kindId));
	}

	@Override
	public Possible<ListEntityResponse> listEntitiesWithKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		if (!kindStore.getKind(kindId).isPresent())
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
		return View.ok(kindValidation.objectsWithKind(kindId)
				.antiIt()
				.map(this::toListEntityItem)
				.wrap(modelFactory::listEntityResponse));
	}

	@Override
	public ListKindResponse listKinds()
	{
		final List<ApiKind> list = kindStore.listAllKinds()
				.map(this::toApiKind)
				.toSortedList((k1, k2) -> k1.url().compareTo(k2.url()));
		return modelFactory.listKindResponse(list, urlTranslation.kindCreateAction());
	}

	@Override
	public Possible<String> postKind(final Kind kind)
	{
		if (kind.kindId() == null)
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		return kindStore.putKind(kind.kindId(), kind, true).map(x -> urlTranslation.kindToUrl(kind.kindId()));
	}
}
