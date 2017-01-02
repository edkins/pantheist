package io.pantheist.api.sql.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.DataAction;

final class ApiSqlRowImpl implements ApiSqlRow
{
	private final DataAction dataAction;

	@Inject
	private ApiSqlRowImpl(@Assisted @JsonProperty("dataAction") final DataAction dataAction)
	{
		this.dataAction = checkNotNull(dataAction);
	}

	@Override
	public DataAction dataAction()
	{
		return dataAction;
	}

}
