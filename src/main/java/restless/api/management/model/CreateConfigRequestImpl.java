package restless.api.management.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class CreateConfigRequestImpl implements CreateConfigRequest
{
	private final String alias;

	private CreateConfigRequestImpl(@Nullable @JsonProperty("alias") final String alias)
	{
		this.alias = alias;
	}

	@Override
	public String alias()
	{
		return alias;
	}

}
