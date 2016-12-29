package restless.api.management.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ApiEntityImpl implements ApiEntity
{
	private final String jsonSchemaUrl;
	private final String javaUrl;

	@Inject
	private ApiEntityImpl(
			@Nullable @Assisted("jsonSchemaUrl") @JsonProperty("jsonSchemaUrl") final String jsonSchemaUrl,
			@Nullable @Assisted("javaUrl") @JsonProperty("javaUrl") final String javaUrl)
	{
		this.jsonSchemaUrl = checkNotNull(jsonSchemaUrl);
		this.javaUrl = checkNotNull(javaUrl);
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

}
