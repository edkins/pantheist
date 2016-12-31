package restless.common.api.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class ListClassifierItemImpl implements ListClassifierItem
{
	private final String url;
	private final String classifierSegment;
	private final boolean suggestHiding;

	@Inject
	private ListClassifierItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("classifierSegment") @JsonProperty("classifierSegment") final String classifierSegment,
			@Assisted("suggestHiding") @JsonProperty("suggestHiding") final boolean suggestHiding)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.classifierSegment = OtherPreconditions.checkNotNullOrEmpty(classifierSegment);
		this.suggestHiding = suggestHiding;
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

	@Override
	public boolean suggestHiding()
	{
		return suggestHiding;
	}

}
