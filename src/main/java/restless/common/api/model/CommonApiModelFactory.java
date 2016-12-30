package restless.common.api.model;

import com.google.inject.assistedinject.Assisted;

public interface CommonApiModelFactory
{
	ListClassifierItem listClassifierItem(
			@Assisted("url") String url,
			@Assisted("classifierSegment") String classifierSegment);

	AdditionalStructureItem additionalStructureItem(
			@Assisted("literal") boolean literal,
			@Assisted("name") String name);
}
