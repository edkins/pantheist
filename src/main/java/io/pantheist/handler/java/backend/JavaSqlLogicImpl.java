package io.pantheist.handler.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.sql.backend.SqlService;

final class JavaSqlLogicImpl implements JavaSqlLogic
{
	private final SqlService sqlService;
	private final CommonSharedModelFactory sharedFactory;
	private final JavaParse javaParse;
	private final ObjectMapper objectMapper;

	@Inject
	private JavaSqlLogicImpl(
			final SqlService sqlService,
			final CommonSharedModelFactory sharedFactory,
			final JavaParse javaParse,
			final ObjectMapper objectMapper)
	{
		this.sqlService = checkNotNull(sqlService);
		this.sharedFactory = checkNotNull(sharedFactory);
		this.javaParse = checkNotNull(javaParse);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public void update(final JavaFileId id, final String code)
	{
		final String qualifiedName = id.qualifiedName();

		final CompilationUnit compilationUnit = javaParse.parse(code);

		final ImmutableList.Builder<String> annotationList = ImmutableList.builder();
		boolean isClass = false;
		boolean isInterface = false;
		final ArrayNode constructors = objectMapper.getNodeFactory().arrayNode();
		if (compilationUnit.getTypes().size() == 1)
		{
			final TypeDeclaration<?> mainType = compilationUnit.getType(0);
			if (mainType instanceof ClassOrInterfaceDeclaration)
			{
				final ClassOrInterfaceDeclaration mainClass = (ClassOrInterfaceDeclaration) mainType;
				isClass = !mainClass.isInterface();
				isInterface = mainClass.isInterface();
			}

			mainType.getAnnotations().forEach(a -> annotationList.add(a.getNameAsString()));

			mainType.getMembers().forEach(m -> {
				if (m instanceof ConstructorDeclaration)
				{
					constructors.add(constructorNode((ConstructorDeclaration) m));
				}
			});
		}

		final TypeInfo constructorsType = sharedFactory.typeInfo(PropertyType.OBJECT_ARRAY, ImmutableMap.of());

		sqlService.updateOrInsert("java-file", "qualifiedName", ImmutableList.of(
				sharedFactory.stringValue("qualifiedName", qualifiedName),
				sharedFactory.booleanValue("isClass", isClass),
				sharedFactory.booleanValue("isInterface", isInterface),
				sharedFactory.stringArrayValue("annotations", annotationList.build()),
				sharedFactory.objectArrayValue("constructors", constructorsType, constructors)));
	}

	private JsonNode constructorNode(final ConstructorDeclaration c)
	{
		final JsonNodeFactory nf = objectMapper.getNodeFactory();
		final ArrayNode parameters = nf.arrayNode();

		c.getParameters().forEach(p -> {
			final ArrayNode annotations = nf.arrayNode();
			p.getAnnotations().forEach(a -> annotations.add(a.getNameAsString()));
			parameters.add(nf.objectNode().set("annotations", annotations));
		});

		return nf.objectNode().set("parameters", parameters);
	}
}
