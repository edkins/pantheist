package io.pantheist.handler.java.backend;

import javax.inject.Inject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

final class JavaParseImpl implements JavaParse
{
	@Inject
	private JavaParseImpl()
	{

	}

	@Override
	public synchronized CompilationUnit parse(final String code)
	{
		return JavaParser.parse(code);
	}

}
