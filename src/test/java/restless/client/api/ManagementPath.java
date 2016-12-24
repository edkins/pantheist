package restless.client.api;

/**
 * An immutable object representing a path into the management api.
 *
 * Most operations can end up throwing a
 * {@link ManagementResourceNotFoundException} if the resource does not exist
 * (and we get a 404 response)
 *
 * They can also throw {@link ManagementUnexpectedResponseException} if the
 * server did not return a response conforming to the management api.
 */
public interface ManagementPath
{
	/**
	 * Appends the given url segment to this path.
	 *
	 * The following escaping is applied:
	 *
	 * - Url path encoding is applied if necessary
	 *
	 * - The initial "+" is prepended to let the management layer know this is a
	 * literal path segment not part of the management api itself
	 *
	 * @param segment
	 *            nonempty path segment
	 * @return
	 */
	ManagementPath segment(String segment);

	/**
	 * Adds a wildcard segment, encoded as a *
	 *
	 * This will match a single segment only.
	 */
	ManagementPath star();

	/**
	 * Returns a path to the data api for this resource. This can be used for
	 * setting and retrieving data for handlers that support that, such as the
	 * filesystem.
	 *
	 * @return data api
	 */
	ManagementData data();

	/**
	 * Returns a path to the config api for this resource. This is used for
	 * setting up any configuration unique to that particular resource path or
	 * path pattern.
	 *
	 * @return config api
	 */
	ManagementConfig config();

	/**
	 * Returns a path to the schema api for this resource. This can be used for
	 * setting validation on json documents. If a schema is set, any future data put
	 * will be validated against the supplied schema.
	 *
	 * Of course the schema itself is validated. Currently only json-schema is supported.
	 *
	 * @return schema api
	 */
	ManagementData schema();
}
