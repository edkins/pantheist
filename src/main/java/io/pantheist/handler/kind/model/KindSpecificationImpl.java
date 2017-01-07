package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

final class KindSpecificationImpl implements KindSpecification
{
	private final JsonNode jsonSchema;
	private final String mimeType;

	public KindSpecificationImpl(
			@Nullable @JsonProperty("jsonSchema") final JsonNode jsonSchema,
			@Nullable @JsonProperty("mimeType") final String mimeType)
	{
		this.jsonSchema = jsonSchema;
		this.mimeType = mimeType;
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

}
