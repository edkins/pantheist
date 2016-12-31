package io.pantheist.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;

final class ApiJavaFileImpl implements ApiJavaFile
{
	private final DataAction dataAction;
	private final DeleteAction deleteAction;
	private final String kindUrl;

	@Inject
	private ApiJavaFileImpl(
			@Assisted @JsonProperty("dataAction") final DataAction dataAction,
			@Assisted @JsonProperty("deleteAction") final DeleteAction deleteAction,
			@Nullable @Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.dataAction = checkNotNull(dataAction);
		this.deleteAction = checkNotNull(deleteAction);
		this.kindUrl = kindUrl;
	}

	@Override
	public DataAction dataAction()
	{
		return dataAction;
	}

	@Override
	public DeleteAction deleteAction()
	{
		return deleteAction;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

}
