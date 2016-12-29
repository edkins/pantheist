package restless.client.api;

public interface ManagementPathJavaPackage
{
	/**
	 * Return the java file with a particular name in this package.
	 */
	ManagementPathJavaFile file(String file);
}
