package restless.client.api;

public interface ManagementPathRoot
{
	/**
	 * Look up a server identified by port.
	 */
	ManagementPathServer server(int port);

	/**
	 * Returns a path to the data api for the given path. This can be used for
	 * setting and retrieving data for handlers that support that, such as the
	 * filesystem.
	 *
	 * @return data api
	 */
	ManagementData data(String path);

	/**
	 * Return a java package resource.
	 */
	ManagementPathJavaPackage javaPackage(String pkg);

	/**
	 * Return the json-schema with the given id.
	 */
	ManagementDataSchema jsonSchema(String schemaId);

	/**
	 * Returns an API for managing an entity.
	 * 
	 * An entity gathers together different kinds of resource (e.g. a json-schema or a java class)
	 * into one concept.
	 */
	ManagementPathEntity entity(String entityId);
}
