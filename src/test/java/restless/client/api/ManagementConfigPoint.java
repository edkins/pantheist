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
	 * Binds this resource to somewhere else on the filesystem
	 *
	 * @param absolutePath Absolute path on the filesystem, starting with slash.
	 */
	void bindToExternalFiles(String absolutePath);

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

	/**
	 * Return whether this configuration point can be found.
	 */
	boolean exists();

	/**
	 * Delete this configuration point.
	 */
	void delete();

	String url();
}
