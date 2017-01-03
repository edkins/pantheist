package io.pantheist.common.api.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface CommonApiModelFactory
{
	ListClassifierItem listClassifierItem(
			@Assisted("url") String url,
			@Assisted("classifierSegment") String classifierSegment,
			@Assisted("suggestHiding") boolean suggestHiding,
			@Assisted("kindUrl") String kindUrl);

	CreateAction createAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType,
			@Nullable @Assisted("urlTemplate") String urlTemplate,
			@Nullable @Assisted("prototypeUrl") String prototypeUrl);

	DataAction dataAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType,
			@Assisted("canPut") boolean canPut);

	ReplaceAction replaceAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType);

	ListClassifierResponse listClassifierResponse(
			List<ListClassifierItem> childResources);

	DeleteAction deleteAction();

	BindingAction bindingAction(@Assisted("url") String url);
}
