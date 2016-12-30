package restless.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.CreateAction;

final class ListJavaPkgResponseImpl implements ListJavaPkgResponse
{
	private final List<ListJavaPkgItem> childResources;
	private final CreateAction createAction;

	@Inject
	private ListJavaPkgResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListJavaPkgItem> childResources,
			@Assisted @JsonProperty("createAction") final CreateAction createAction)
	{
		this.childResources = ImmutableList.copyOf(childResources);
		this.createAction = checkNotNull(createAction);
	}

	@Override
	public List<ListJavaPkgItem> childResources()
	{
		return childResources;
	}

	@Override
	public CreateAction createAction()
	{
		return createAction;
	}

}
