package restless.handler.java.backend;

import restless.handler.binding.backend.PossibleData;

public interface JavaStore
{
	/**
	 * Given a java source file, store it where it belongs. This can be determined by looking
	 * at the package declaration at the start of the file, and the main class name.
	 *
	 * It will return ALREADY_EXISTS if there's already a java class in that package with
	 * that name.
	 *
	 * It will return REQUEST_HAS_INVALID_SYNTAX if it seems to be invalid java syntax.
	 *
	 * If there's something else we don't like about it, we'll return REQUEST_FAILED_SCHEMA.
	 *
	 * @return fully qualified type name, or else some kind of error code
	 */
	PossibleData storeJava(String code);

	/**
	 * Return a java file based on its fully qualified type name
	 */
	PossibleData getJava(String qualifiedTypeName);
}
