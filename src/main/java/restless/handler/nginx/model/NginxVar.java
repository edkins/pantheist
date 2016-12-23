package restless.handler.nginx.model;

import java.io.File;

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
	 * Set the value to the absolute path of the file given.
	 */
	void giveFile(File file);
}
