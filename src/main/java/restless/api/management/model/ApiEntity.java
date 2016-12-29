package restless.api.management.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ApiEntityImpl.class)
public interface ApiEntity
{
	@Nullable
	@JsonProperty("kindUrl")
	String kindUrl();

	@Nullable
	@JsonProperty("jsonSchemaUrl")
	String jsonSchemaUrl();

	@Nullable
	@JsonProperty("javaUrl")
	String javaUrl();

	/**
	 * Return whether the entity is valid according to the kind.
	 *
	 * Ignored on input.
	 */
	@JsonProperty("valid")
	boolean valid();
}
