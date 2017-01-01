package io.pantheist.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.handler.kind.model.JavaClause;
import io.pantheist.handler.kind.model.KindLevel;

public interface ApiKindModelFactory
{
	ApiKind kind(
			@Nullable List<ListClassifierItem> childResources,
			@Nullable @Assisted("kindId") String kindId,
			KindLevel level,
			@Assisted("discoverable") Boolean discoverable,
			@Nullable JavaClause java,
			@Assisted("partOfSystem") boolean partOfSystem);

	ListKindResponse listKindResponse(
			List<ListKindItem> childResources,
			CreateAction createAction);

	ListKindItem listKindItem(
			@Assisted("url") String url,
			@Assisted("kindUrl") String kindUrl);
}
