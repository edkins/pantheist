package restless.api.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import restless.common.util.OtherPreconditions;

final class CreateConfigRequestImpl implements CreateConfigRequest
{
	private final String pathSpec;

	private CreateConfigRequestImpl(@JsonProperty("pathSpec") final String pathSpec)
	{
		this.pathSpec = OtherPreconditions.checkNotNullOrEmpty(pathSpec);
	}

	@Override
	public String pathSpec()
	{
		return pathSpec;
	}

}
