package restless.client.api;

public interface ManagementData
{
	/**
	 * Retrieve the data for this resource as a string
	 *
	 * @param contentType the expected content type
	 * @return data as a string
	 * @throws Man
	 *             if this resource does not support retrieving data directly
	 *             through the management api.
	 */
	String getString(String contentType);

	/**
	 * Puts the data for this resource
	 *
	 * @param data
	 *            string to put
	 * @throws UnsupportedOperationException
	 *             if this resource does not support putting data directly
	 *             through the management api.
	 */
	void putString(String data);

	/**
	 * Convenience method for testing: puts the file identified by the given resource path.
	 *
	 * The contentType is what we say in the http header. It won't affect how we actually
	 * transmit the data, so make sure the resource is of the right type.
	 */
	void putResource(String resourcePath, String contentType);

	/**
	 * Attempt to put a resource and return the response type.
	 */
	ResponseType putResourceResponseType(String resourcePath, String contentType);

	/**
	 * Returns the class of response that we receive when we call GET on this
	 * data resource.
	 */
	ResponseType getResponseTypeForContentType(String jsonSchemaMime);
}
