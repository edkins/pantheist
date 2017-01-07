package io.pantheist.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.model.Kind;

final class KindBackendImpl implements KindBackend
{
	private final KindStore kindStore;
	private final UrlTranslation urlTranslation;
	private final FileKindHandler fileKindHandler;
	private final ObjectMapper objectMapper;

	@Inject
	private KindBackendImpl(
			final KindStore kindStore,
			final UrlTranslation urlTranslation,
			final FileKindHandler fileKindHandler,
			final ObjectMapper objectMapper)
	{
		this.kindStore = checkNotNull(kindStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.fileKindHandler = checkNotNull(fileKindHandler);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public Possible<String> postKind(final Kind kind)
	{
		if (kind.kindId() == null)
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		final String text;
		try
		{
			text = objectMapper.writeValueAsString(kind);
		}
		catch (final JsonProcessingException e)
		{
			throw Throwables.propagate(e);
		}
		return fileKindHandler.putEntity(metakind(), kind.kindId(), text, true).map(x -> {
			return urlTranslation.kindToUrl(kind.kindId());
		});
	}

	private Kind metakind()
	{
		return fileKindHandler.getKind("kind").get();
	}

	@Override
	public Possible<String> newInstanceOfKind(final String kindId)
	{
		final Optional<Kind> kind = fileKindHandler.getKind(kindId);
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
