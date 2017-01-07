package io.pantheist.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.pantheist.api.kind.model.ApiKindModelFactory;
import io.pantheist.api.kind.model.ListKindItem;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.api.model.KindedImpl;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.model.Kind;

final class KindBackendImpl implements KindBackend
{
	private final KindStore kindStore;
	private final ApiKindModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final FileKindHandler fileKindHandler;

	@Inject
	private KindBackendImpl(
			final KindStore kindStore,
			final ApiKindModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final FileKindHandler fileKindHandler)
	{
		this.kindStore = checkNotNull(kindStore);
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
				return fileKindHandler.newInstanceOfKind(kind.get());
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
