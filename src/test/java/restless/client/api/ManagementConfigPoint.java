package restless.client.api;

public interface ManagementConfigPoint
{
	/**
	 * Binds this particular resource to somewhere in the filesystem. Currently
	 * the files will go in the same place specified by the path.
	 *
	 * @param path to bind
	 */
	void bindToFilesystem();

	/**
	 * Binds this resource to one of the resource files.
	 *
	 * @param resourcePath subset of resource files to expose, or "" if you want them all.
	 */
	void bindToResourceFiles(String resourcePath);

	/**
	 * Returns a path to the schema api for this resource. This can be used for
	 * setting validation on json documents. If a schema is set, any future data put
	 * will be validated against the supplied schema.
	 *
	 * Of course the schema itself is validated. Currently only json-schema is supported.
	 */
	ManagementData schema();

	/**
	 * Returns a path representing a jersey resource, i.e. a java source file. You can put
	 * the java source directly here.
	 *
	 * @return jersey-file api
	 */
	ManagementData jerseyFile();
}
