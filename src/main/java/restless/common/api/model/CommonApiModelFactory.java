package restless.common.api.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface CommonApiModelFactory
{
	ListClassifierItem listClassifierItem(
			@Assisted("url") String url,
			@Assisted("classifierSegment") String classifierSegment,
			@Assisted("suggestHiding") boolean suggestHiding);

	AdditionalStructureItem additionalStructureItem(
			@Assisted("literal") boolean literal,
			@Assisted("name") String name,
			@Assisted("suggestHiding") boolean suggestHiding);

	CreateAction createAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType,
			@Nullable List<AdditionalStructureItem> additionalStructure);

	DataAction dataAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType,
			@Assisted("canPut") boolean canPut);

	ListClassifierResponse listClassifierResponse(
			List<ListClassifierItem> childResources);

	DeleteAction deleteAction();

	BindingAction bindingAction(@Assisted("url") String url);
}
