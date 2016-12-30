package restless.handler.uri;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

public class ListClassifierItemImpl implements ListClassifierItem
{
	private final String url;
	private final String classifierSegment;

	@Inject
	private ListClassifierItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("classifierSegment") @JsonProperty("classifierSegment") final String classifierSegment)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.classifierSegment = OtherPreconditions.checkNotNullOrEmpty(classifierSegment);
	}

	@Override
	public String url()
	{
		return url;
	}

	@Override
	public String classifierSegment()
	{
		return classifierSegment;
	}

}
