package restless.client.api;

public interface ManagementData
{
	/**
	 * Retrieve the data for this resource as a string
	 *
	 * @return data as a string
	 * @throws Man
	 *             if this resource does not support retrieving data directly
	 *             through the management api.
	 */
	String getString();

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
}
