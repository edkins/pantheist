package restless.handler.uri;

import com.google.inject.assistedinject.Assisted;

interface HandlerUriModelFactory
{
	ListClassifierItem listClassifierItem(
			@Assisted("url") String url,
			@Assisted("classifierSegment") String classifierSegment);
}
