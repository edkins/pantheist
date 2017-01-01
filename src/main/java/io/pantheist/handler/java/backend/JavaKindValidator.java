package io.pantheist.handler.java.backend;

import com.github.javaparser.ast.CompilationUnit;

import io.pantheist.handler.kind.model.JavaClause;

interface JavaKindValidator
{
	boolean validateKind(CompilationUnit compilationUnit, JavaClause javaClause);
}
