package restless.handler.entity.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.handler.java.model.JavaFileId;

@JsonDeserialize(as = EntityImpl.class)
public interface Entity
{
	@JsonProperty("discovered")
	boolean discovered();

	@Nullable
	@JsonProperty("kindId")
	String kindId();

	@Nullable
	@JsonProperty("jsonSchemaId")
	String jsonSchemaId();

	@Nullable
	@JsonProperty("javaFileId")
	JavaFileId javaFileId();
}
