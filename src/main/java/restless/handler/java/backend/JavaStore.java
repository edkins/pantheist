package restless.handler.java.backend;

import restless.common.util.Possible;

public interface JavaStore
{
	/**
	 * Given a java source file, store it in the given package. This must agree with the
	 * package declaration in the java code.
	 *
	 * It will return REQUEST_HAS_INVALID_SYNTAX if it seems to be invalid java syntax.
	 *
	 * If there's something else we don't like about it, we'll return REQUEST_FAILED_SCHEMA.
	 */
	Possible<Void> putJava(String pkg, String file, String code);

	/**
	 * Return a java file
	 */
	Possible<String> getJava(String pkg, String file);
}
