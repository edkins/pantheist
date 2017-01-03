package io.pantheist.api.flatdir.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.util.OtherPreconditions;

final class ApiFlatDirFileImpl implements ApiFlatDirFile
{
	private final DataAction dataAction;
	private final String kindUrl;

	@Inject
	private ApiFlatDirFileImpl(@Assisted @JsonProperty("dataAction") final DataAction dataAction,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.dataAction = checkNotNull(dataAction);
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
	}

	@Override
	public DataAction dataAction()
	{
		return dataAction;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

}
