package io.pantheist.handler.java.backend;

import com.github.javaparser.ast.CompilationUnit;

/**
 * I'm not convinced the static {@link JavaParse#parse(String)} method is thread safe,
 * so I've wrapped it here.
 */
interface JavaParse
{
	CompilationUnit parse(String code);
}
