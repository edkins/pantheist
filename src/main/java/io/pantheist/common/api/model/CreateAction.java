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

	@Nullable
	@JsonProperty("jsonSchema")
	String jsonSchema();

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

	/**
	 * Whether to use PUT or POST for creating these items.
	 *
	 * If you use POST it's assumed that certain parts of the path will be missing, e.g. an id.
	 * This will instead either be generated automatically or inferred from the contents of what
	 * you're posting (such as java package and class name). POST will return 201 Created, with
	 * the location of the new resource in a Location header.
	 */
	@JsonProperty("method")
	HttpMethod method();
}
