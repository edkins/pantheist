package restless.api.management.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ApiEntityImpl implements ApiEntity
{
	private final String kindUrl;
	private final String jsonSchemaUrl;
	private final String javaUrl;
	private final boolean valid;

	@Inject
	private ApiEntityImpl(
			@Nullable @Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl,
			@Nullable @Assisted("jsonSchemaUrl") @JsonProperty("jsonSchemaUrl") final String jsonSchemaUrl,
			@Nullable @Assisted("javaUrl") @JsonProperty("javaUrl") final String javaUrl,
			@Assisted("valid") @JsonProperty("valid") final boolean valid)
	{
		this.kindUrl = kindUrl;
		this.jsonSchemaUrl = jsonSchemaUrl;
		this.javaUrl = javaUrl;
		this.valid = valid;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

	@Override
	public String jsonSchemaUrl()
	{
		return jsonSchemaUrl;
	}

	@Override
	public String javaUrl()
	{
		return javaUrl;
	}

	@Override
	public boolean valid()
	{
		return valid;
	}

}
