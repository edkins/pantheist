package restless.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.BindingAction;
import restless.common.api.model.CreateAction;

final class ListJavaPkgResponseImpl implements ListJavaPkgResponse
{
	private final List<ListJavaPkgItem> childResources;
	private final CreateAction createAction;
	private final BindingAction bindingAction;

	@Inject
	private ListJavaPkgResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListJavaPkgItem> childResources,
			@Assisted @JsonProperty("createAction") final CreateAction createAction,
			@Assisted @JsonProperty("bindingAction") final BindingAction bindingAction)
	{
		this.childResources = ImmutableList.copyOf(childResources);
		this.createAction = checkNotNull(createAction);
		this.bindingAction = checkNotNull(bindingAction);
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

	@Override
	public BindingAction bindingAction()
	{
		return bindingAction;
	}

}
