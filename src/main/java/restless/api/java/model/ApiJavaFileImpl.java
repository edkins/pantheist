package restless.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.DataAction;
import restless.common.api.model.DeleteAction;

final class ApiJavaFileImpl implements ApiJavaFile
{
	private final DataAction dataAction;
	private final DeleteAction deleteAction;

	@Inject
	private ApiJavaFileImpl(
			@Assisted @JsonProperty("dataAction") final DataAction dataAction,
			@Assisted @JsonProperty("deleteAction") final DeleteAction deleteAction)
	{
		this.dataAction = checkNotNull(dataAction);
		this.deleteAction = checkNotNull(deleteAction);
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

}
