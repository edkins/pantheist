package restless.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.DataAction;

final class ApiJavaFileImpl implements ApiJavaFile
{
	private final DataAction dataAction;

	@Inject
	private ApiJavaFileImpl(@Assisted @JsonProperty("dataAction") final DataAction dataAction)
	{
		this.dataAction = checkNotNull(dataAction);
	}

	@Override
	public DataAction dataAction()
	{
		return dataAction;
	}

}
