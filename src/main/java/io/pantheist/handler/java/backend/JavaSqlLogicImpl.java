package io.pantheist.handler.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Pair;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.handler.sql.model.SqlProperty;

final class JavaSqlLogicImpl implements JavaSqlLogic
{
	private static final Logger LOGGER = LogManager.getLogger(JavaSqlLogicImpl.class);
	private static final String JAVA_FILE = "java-file";
	private final SqlService sqlService;
	private final JavaParse javaParse;
	private final ObjectMapper objectMapper;
	private final KindStore kindStore;

	@Inject
	private JavaSqlLogicImpl(
			final SqlService sqlService,
			final JavaParse javaParse,
			final ObjectMapper objectMapper,
			final KindStore kindStore)
	{
		this.sqlService = checkNotNull(sqlService);
		this.javaParse = checkNotNull(javaParse);
		this.objectMapper = checkNotNull(objectMapper);
		this.kindStore = checkNotNull(kindStore);
	}

	@Override
	public void update(final AntiIterator<Pair<JavaFileId, String>> codeWithIds)
	{
		final List<SqlProperty> columns = kindStore.listSqlPropertiesOfKind(JAVA_FILE).toList();

		sqlService.updateOrInsert(JAVA_FILE, columns, codeWithIds.map(this::codeNode));
	}

	private ObjectNode codeNode(final Pair<JavaFileId, String> codeWithId)
	{
		final JavaFileId id = codeWithId.first();
		final String qualifiedName = id.qualifiedName();
		final JsonNodeFactory nf = objectMapper.getNodeFactory();
		final ArrayNode annotationList = nf.arrayNode();
		boolean isClass = false;
		boolean isInterface = false;
		final ArrayNode constructors = nf.arrayNode();

		try
		{
			final CompilationUnit compilationUnit = javaParse.parse(codeWithId.second());

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
		}
		catch (final ParseProblemException e)
		{
			LOGGER.catching(e);
		}

		final ObjectNode obj = nf.objectNode();

		obj.put("qualifiedName", qualifiedName);
		obj.put("package", id.pkg());
		obj.put("fileName", id.file());
		obj.put("isClass", isClass);
		obj.put("isInterface", isInterface);
		obj.replace("annotations", annotationList);
		obj.replace("constructors", constructors);

		return obj;
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
