package restless.common.api.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface CommonApiModelFactory
{
	ListClassifierItem listClassifierItem(
			@Assisted("url") String url,
			@Assisted("classifierSegment") String classifierSegment);

	AdditionalStructureItem additionalStructureItem(
			@Assisted("literal") boolean literal,
			@Assisted("name") String name);

	CreateAction createAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType,
			List<AdditionalStructureItem> additionalStructure);

	DataAction dataAction(
			BasicContentType basicType,
			@Assisted("mimeType") String mimeType,
			@Assisted("canPut") boolean canPut);

	ListClassifierResponse listClassifierResponse(
			List<ListClassifierItem> childResources);
}
