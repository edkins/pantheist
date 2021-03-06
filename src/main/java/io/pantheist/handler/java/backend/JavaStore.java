package io.pantheist.handler.java.backend;

import java.util.Optional;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.java.model.JavaBinding;
import io.pantheist.handler.java.model.JavaComponent;
import io.pantheist.handler.java.model.JavaFileId;

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
	 * Detect package and filename from the code itself. Create it at that location,
	 * and return the id representing the package and filename.
	 */
	Possible<JavaFileId> postJava(String code, boolean failIfExists);

	/**
	 * Return a java file
	 */
	Possible<String> getJava(JavaFileId fileId);

	boolean fileExists(JavaFileId fileId);

	Optional<JavaComponent> getJavaComponent(JavaFileId fileId, String componentId);

	Optional<JavaFileId> findFileByName(String fileName);

	AntiIterator<JavaFileId> allJavaFiles();

	/**
	 * Returns true if the package path exists and contains java files.
	 *
	 * Will not return true if it's only subpackages that exist.
	 */
	boolean packageExists(String pkg);

	AntiIterator<JavaFileId> filesInPackage(String pkg);

	/**
	 * Delete a file.
	 *
	 * @return whether the file previously existed.
	 */
	boolean deleteFile(JavaFileId fileId);

	void setJavaBinding(JavaBinding binding);

	JavaBinding getJavaBinding();

	void registerFilesInSql();
}
