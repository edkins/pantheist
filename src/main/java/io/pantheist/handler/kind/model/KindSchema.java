package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = KindSchemaImpl.class)
public interface KindSchema
{
	@Nullable
	@JsonProperty("java")
	JavaClause java();

	@JsonProperty("subKindOf")
	List<String> subKindOf();

	@Nullable
	@JsonProperty("properties")
	List<KindProperty> properties();
}
