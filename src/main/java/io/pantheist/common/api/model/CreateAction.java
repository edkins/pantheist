package io.pantheist.common.api.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = CreateActionImpl.class)
public interface CreateAction
{
	@JsonProperty("basicType")
	BasicContentType basicType();

	@JsonProperty("mimeType")
	String mimeType();

	/**
	 * Returns a url template for creating instances of this kind.
	 *
	 * Variables will be enclosed in braces like so:
	 *
	 * http://localhost:3142/thing/{thingId}
	 */
	@JsonProperty("urlTemplate")
	String urlTemplate();

	/**
	 * Specifies a resource which serves as a prototype for creating new
	 * instances of this kind.
	 *
	 * It's a "blank" example of what you're trying to create. It won't
	 * contain any template parameters.
	 */
	@Nullable
	@JsonProperty("prototypeUrl")
	String prototypeUrl();
}
