package io.pantheist.handler.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.sql.backend.SqlService;

final class JavaSqlLogicImpl implements JavaSqlLogic
{
	private final SqlService sqlService;
	private final JavaParse javaParse;
	private final ObjectMapper objectMapper;

	@Inject
	private JavaSqlLogicImpl(
			final SqlService sqlService,
			final JavaParse javaParse,
			final ObjectMapper objectMapper)
	{
		this.sqlService = checkNotNull(sqlService);
		this.javaParse = checkNotNull(javaParse);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public void update(final JavaFileId id, final String code)
	{
		final String qualifiedName = id.qualifiedName();

		final CompilationUnit compilationUnit = javaParse.parse(code);
		final JsonNodeFactory nf = objectMapper.getNodeFactory();

		final ArrayNode annotationList = nf.arrayNode();
		boolean isClass = false;
		boolean isInterface = false;
		final ArrayNode constructors = nf.arrayNode();
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

		final ObjectNode obj = nf.objectNode();

		obj.put("qualifiedName", qualifiedName);
		obj.put("isClass", isClass);
		obj.put("isInterface", isInterface);
		obj.replace("annotations", annotationList);
		obj.replace("constructors", constructors);

		sqlService.updateOrInsert("java-file", "qualifiedName", obj);
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
