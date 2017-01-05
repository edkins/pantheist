package io.pantheist.common.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class CreateActionImpl implements CreateAction
{
	private final BasicContentType basicType;
	private final String mimeType;
	private final String urlTemplate;
	private final String prototypeUrl;
	private final HttpMethod method;
	private final String jsonSchema;

	@Inject
	private CreateActionImpl(
			@Assisted @JsonProperty("basicType") final BasicContentType basicType,
			@Assisted("mimeType") @JsonProperty("mimeType") final String mimeType,
			@Nullable @Assisted("jsonSchema") @JsonProperty("jsonSchema") final String jsonSchema,
			@Nullable @Assisted("urlTemplate") @JsonProperty("urlTemplate") final String urlTemplate,
			@Nullable @Assisted("prototypeUrl") @JsonProperty("prototypeUrl") final String prototypeUrl,
			@Assisted @JsonProperty("method") final HttpMethod method)
	{
		this.basicType = checkNotNull(basicType);
		this.mimeType = OtherPreconditions.checkNotNullOrEmpty(mimeType);
		this.jsonSchema = jsonSchema;
		this.urlTemplate = OtherPreconditions.checkNotNullOrEmpty(urlTemplate);
		this.prototypeUrl = prototypeUrl;
		this.method = checkNotNull(method);
	}

	@Override
	public BasicContentType basicType()
	{
		return basicType;
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}

	@Override
	public String prototypeUrl()
	{
		return prototypeUrl;
	}

	@Override
	public String urlTemplate()
	{
		return urlTemplate;
	}

	@Override
	public HttpMethod method()
	{
		return method;
	}

	@Override
	public String jsonSchema()
	{
		return jsonSchema;
	}

}
