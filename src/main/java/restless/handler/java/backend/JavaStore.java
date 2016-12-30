package restless.handler.java.backend;

import java.util.Optional;

import restless.common.util.AntiIterator;
import restless.common.util.Possible;
import restless.handler.java.model.JavaComponent;
import restless.handler.java.model.JavaFileId;
import restless.handler.kind.model.JavaClause;

public interface JavaStore
{
	/**
	 * Given a java source file, store it in the given package. This must agree with the
	 * package declaration in the java code.
	 *
	 * It will return REQUEST_HAS_INVALID_SYNTAX if it seems to be invalid java syntax.
	 *
	 * If there's something else we don't like about it, we'll return REQUEST_FAILED_SCHEMA.
	 *
	 * If you set failIfExists to true then it can fail with ALREADY_EXISTS.
	 */
	Possible<Void> putJava(JavaFileId fileId, String code, boolean failIfExists);

	/**
	 * Return a java file
	 */
	Possible<String> getJava(JavaFileId fileId);

	boolean fileExists(JavaFileId fileId);

	Optional<JavaComponent> getJavaComponent(JavaFileId fileId, String componentId);

	boolean validateKind(JavaFileId fileId, JavaClause javaClause);

	Optional<JavaFileId> findFileByName(String fileName);

	AntiIterator<JavaFileId> allJavaFiles();

	/**
	 * Returns true if the package path exists and contains java files.
	 *
	 * Will not return true if it's only subpackages that exist.
	 */
	boolean packageExists(String pkg);

	AntiIterator<JavaFileId> filesInPackage(String pkg);
}
