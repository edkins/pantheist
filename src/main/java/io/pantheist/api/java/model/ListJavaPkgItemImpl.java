package io.pantheist.api.java.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListJavaPkgItemImpl implements ListJavaPkgItem
{
	private final String url;

	@Inject
	private ListJavaPkgItemImpl(@Assisted("url") @JsonProperty("url") final String url)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
	}

	@Override
	public String url()
	{
		return url;
	}

}
