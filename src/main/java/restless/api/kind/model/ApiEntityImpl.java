package restless.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.handler.uri.ListClassifierItem;

final class ApiEntityImpl implements ApiEntity
{
	private final boolean discovered;
	private final String kindUrl;
	private final String jsonSchemaUrl;
	private final String javaUrl;
	private final boolean valid;
	private final List<? extends ListClassifierItem> childResources;

	@Inject
	private ApiEntityImpl(
			@Assisted("discovered") @JsonProperty("discovered") final boolean discovered,
			@Nullable @Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl,
			@Nullable @Assisted("jsonSchemaUrl") @JsonProperty("jsonSchemaUrl") final String jsonSchemaUrl,
			@Nullable @Assisted("javaUrl") @JsonProperty("javaUrl") final String javaUrl,
			@Assisted("valid") @JsonProperty("valid") final boolean valid,
			@Nullable @Assisted @JsonProperty("childResources") final List<ListClassifierItem> childResources)
	{
		this.discovered = discovered;
		this.kindUrl = kindUrl;
		this.jsonSchemaUrl = jsonSchemaUrl;
		this.javaUrl = javaUrl;
		this.valid = valid;
		this.childResources = childResources;
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

	@Override
	public boolean discovered()
	{
		return discovered;
	}

	@Override
	public List<? extends ListClassifierItem> childResources()
	{
		return childResources;
	}

}
