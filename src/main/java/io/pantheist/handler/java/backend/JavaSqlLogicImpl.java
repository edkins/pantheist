package io.pantheist.handler.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ImmutableList;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.sql.backend.SqlService;

final class JavaSqlLogicImpl implements JavaSqlLogic
{
	private final SqlService sqlService;
	private final CommonSharedModelFactory sharedFactory;
	private final JavaParse javaParse;

	@Inject
	private JavaSqlLogicImpl(
			final SqlService sqlService,
			final CommonSharedModelFactory sharedFactory,
			final JavaParse javaParse)
	{
		this.sqlService = checkNotNull(sqlService);
		this.sharedFactory = checkNotNull(sharedFactory);
		this.javaParse = checkNotNull(javaParse);
	}

	@Override
	public void update(final JavaFileId id, final String code)
	{
		final String qualifiedName = id.qualifiedName();

		final CompilationUnit compilationUnit = javaParse.parse(code);

		boolean isClass = false;
		boolean isInterface = false;
		if (compilationUnit.getTypes().size() == 1)
		{
			final TypeDeclaration<?> mainType = compilationUnit.getType(0);
			if (mainType instanceof ClassOrInterfaceDeclaration)
			{
				final ClassOrInterfaceDeclaration mainClass = (ClassOrInterfaceDeclaration) mainType;
				isClass = !mainClass.isInterface();
				isInterface = mainClass.isInterface();
			}
		}

		sqlService.updateOrInsert("java-file", "qualifiedName", ImmutableList.of(
				sharedFactory.stringValue("qualifiedName", qualifiedName),
				sharedFactory.booleanValue("isClass", isClass),
				sharedFactory.booleanValue("isInterface", isInterface),
				sharedFactory.arrayStringValue("annotations", ImmutableList.of())));
	}
}
