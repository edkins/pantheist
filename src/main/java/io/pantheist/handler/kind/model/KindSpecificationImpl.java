package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

final class KindSpecificationImpl implements KindSpecification
{
	private final JsonNode jsonSchema;
	private final String mimeType;
	private final List<KindHook> hooks;

	public KindSpecificationImpl(
			@Nullable @JsonProperty("jsonSchema") final JsonNode jsonSchema,
			@Nullable @JsonProperty("mimeType") final String mimeType,
			@Nullable @JsonProperty("hooks") final List<KindHook> hooks)
	{
		this.jsonSchema = jsonSchema;
		this.mimeType = mimeType;
		this.hooks = hooks;
	}

	@Override
	public JsonNode jsonSchema()
	{
		return jsonSchema;
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("jsonSchema", jsonSchema)
				.add("mimeType", mimeType)
				.toString();
	}

	@Override
	public List<KindHook> hooks()
	{
		return hooks;
	}

}
