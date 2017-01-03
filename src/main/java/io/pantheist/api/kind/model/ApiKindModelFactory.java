package io.pantheist.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.api.model.Presentation;
import io.pantheist.common.api.model.ReplaceAction;
import io.pantheist.handler.kind.model.KindSchema;

public interface ApiKindModelFactory
{
	ApiKind kind(
			@Nullable List<ListClassifierItem> childResources,
			ReplaceAction replaceAction,
			@Nullable @Assisted("kindId") String kindId,
			KindSchema schema,
			@Assisted("partOfSystem") boolean partOfSystem,
			@Assisted("instancePresentation") Presentation instancePresentation,
			CreateAction createAction);

	ListKindResponse listKindResponse(
			List<ListKindItem> childResources,
			CreateAction createAction);

	ListKindItem listKindItem(
			@Assisted("url") String url,
			@Assisted("kindUrl") String kindUrl,
			@Assisted("instancePresentation") Presentation instancePresentation);

	ListEntityItem listEntityItem(
			@Assisted("url") String url,
			@Assisted("entityId") String entityId,
			@Assisted("kindUrl") String kindUrl);

	ListEntityResponse listEntityResponse(List<ListEntityItem> childResources);
}
