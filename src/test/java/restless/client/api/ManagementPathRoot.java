package restless.client.api;

import restless.api.kind.model.ListEntityResponse;
import restless.api.management.model.ListClassifierResponse;
import restless.api.management.model.ListJavaPkgResponse;

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

	/**
	 * Returns an API for managing a kind.
	 *
	 * A kind is like a schema for entities. It specifies which handlers must be present
	 * and some restrictions on the structure of their associated files.
	 *
	 * For example, it might say that the java handler needs to be present and
	 * its associated file must be an interface, not a class.
	 */
	ManagementPathKind kind(String kindId);

	ListEntityResponse listEntities();

	ListClassifierResponse listClassifiers();

	String urlOfService(String classifierSegment);

	ListJavaPkgResponse listJavaPackages();
}
