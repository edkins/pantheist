package io.pantheist.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.api.kind.model.ApiKindModelFactory;
import io.pantheist.api.kind.model.ListEntityItem;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.api.kind.model.ListKindItem;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.api.model.KindedImpl;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.backend.KindValidation;
import io.pantheist.handler.kind.model.Kind;

final class KindBackendImpl implements KindBackend
{
	private final KindStore kindStore;
	private final KindValidation kindValidation;
	private final ApiKindModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final FileKindHandler fileKindHandler;

	@Inject
	private KindBackendImpl(
			final KindStore kindStore,
			final KindValidation kindValidation,
			final ApiKindModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final FileKindHandler fileKindHandler)
	{
		this.kindStore = checkNotNull(kindStore);
		this.kindValidation = checkNotNull(kindValidation);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.fileKindHandler = checkNotNull(fileKindHandler);
	}

	private ListKindItem toListKindItem(final Kind k)
	{
		final String kindId = k.kindId();
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		final String url = urlTranslation.kindToUrl(kindId);
		final String kindUrl = metakind();
		return modelFactory.listKindItem(
				url,
				k.kindId(),
				kindUrl);
	}

	private String metakind()
	{
		return urlTranslation.kindToUrl("kind");
	}

	@Override
	public Possible<Kinded<Kind>> getKind(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindStore.getKind(kindId)
				.map(k -> KindedImpl.of(metakind(), k))
				.map(View::ok)
				.orElse(FailureReason.DOES_NOT_EXIST.happened());
	}

	@Override
	public Possible<Void> putKindData(final String kindId, final Kind kind, final boolean failIfExists)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		checkNotNull(kind);
		if (kind.kindId() == null)
		{
			kind.setKindId(kindId);
		}
		else if (!kind.kindId().equals(kindId))
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		return kindStore.putKind(kindId, kind, failIfExists);
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
		final List<ListKindItem> list = kindStore.listAllKinds()
				.map(this::toListKindItem)
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

	@Override
	public Possible<String> newInstanceOfKind(final String kindId)
	{
		final Optional<Kind> kind = kindStore.getKind(kindId);
		if (kind.isPresent())
		{
			if (kind.get().hasParent("file"))
			{
				return View.ok(fileKindHandler.newInstanceOfKind(kind.get()));
			}
			else
			{
				return FailureReason.HANDLER_DOES_NOT_SUPPORT.happened();
			}
		}
		else
		{
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<KindedMime> getEntity(final String kindId, final String entityId)
	{
		final Optional<Kind> kind = kindStore.getKind(kindId);
		if (kind.isPresent())
		{
			if (kindStore.derivesFrom(kind.get(), "file"))
			{
				return fileKindHandler.getEntity(kind.get(), entityId);
			}
			else
			{
				return FailureReason.HANDLER_DOES_NOT_SUPPORT.happened();
			}
		}
		else
		{
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}
}
