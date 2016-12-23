package restless.handler.nginx.model;

import restless.handler.filesystem.backend.FsPath;

public interface NginxVar
{
	/**
	 * Represent this as an nginx configuration file.
	 */
	@Override
	String toString();

	/**
	 * @return the value of this configuration variable
	 */
	String value();

	/**
	 * Set the value of this configuration variable
	 *
	 * @throws IllegalStateException
	 *             if already set.
	 */
	void giveValue(String value);

	/**
	 * Set the value to the absolute path of the directory given, which is
	 * assumed to lie within the data directory.
	 *
	 * The difference between this and giveFilePath is this one will put a slash
	 * at the end.
	 */
	void giveDirPath(FsPath path);

	/**
	 * Set the value to the absolute path of the file given, which is assumed to
	 * lie within the data directory.
	 */
	void giveFilePath(FsPath path);
}
