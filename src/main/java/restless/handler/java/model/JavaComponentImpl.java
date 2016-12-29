package restless.handler.java.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class JavaComponentImpl implements JavaComponent
{
	private final boolean isRoot;

	@Inject
	private JavaComponentImpl(@Assisted("isRoot") @JsonProperty("isRoot") final boolean isRoot)
	{
		this.isRoot = isRoot;
	}

	@Override
	public boolean isRoot()
	{
		return isRoot;
	}

}
