package io.pantheist.common.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = TypeInfoImpl.class)
public interface TypeInfo
{
	/**
	 * For now, cannot be "array".
	 */
	@JsonProperty("type")
	PropertyType type();

	// leaving out "items" for now. This means arrays can only contain simple types.
}
