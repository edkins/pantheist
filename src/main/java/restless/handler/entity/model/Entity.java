package restless.handler.entity.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
	@JsonProperty("javaPkg")
	String javaPkg();

	@Nullable
	@JsonProperty("javaFile")
	String javaFile();
}
