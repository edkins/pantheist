package io.pantheist.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.KindPresentation;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.handler.kind.model.KindSchema;

public interface ApiKindModelFactory
{
	ApiKind kind(
			@Assisted("url") String url,
			@Assisted("kindUrl") String kindUrl,
			@Nullable List<ListClassifierItem> childResources,
			DataAction dataAction,
			@Assisted("kindId") String kindId,
			KindSchema schema,
			@Assisted("partOfSystem") boolean partOfSystem,
			@Assisted KindPresentation instancePresentation,
			CreateAction createAction,
			DeleteAction deleteAction);

	ListKindResponse listKindResponse(
			List<ApiKind> childResources,
			CreateAction createAction);

	ListEntityItem listEntityItem(
			@Assisted("url") String url,
			@Assisted("entityId") String entityId,
			@Assisted("kindUrl") String kindUrl);

	ListEntityResponse listEntityResponse(List<ListEntityItem> childResources);
}
