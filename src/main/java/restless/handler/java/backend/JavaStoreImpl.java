package restless.handler.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import restless.common.util.OtherPreconditions;
import restless.handler.binding.backend.PossibleData;
import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;

final class JavaStoreImpl implements JavaStore
{
	private static final Logger LOGGER = LogManager.getLogger(JavaStoreImpl.class);
	private final FilesystemStore filesystem;

	@Inject
	private JavaStoreImpl(final FilesystemStore filesystem)
	{
		this.filesystem = checkNotNull(filesystem);
	}

	private FsPath bucket()
	{
		return filesystem.systemBucket().segment("java");
	}

	private FsPath pathToType(final String typeName)
	{
		OtherPreconditions.checkNotNullOrEmpty(typeName);
		FsPath path = bucket();
		if (typeName.startsWith(".") || typeName.contains("..") || typeName.endsWith("."))
		{
			throw new IllegalArgumentException("Bad type name: " + typeName);
		}
		final String[] segments = typeName.split("\\.");
		for (int i = 0; i < segments.length; i++)
		{
			String segment = segments[i];
			if (i == segments.length - 1)
			{
				segment = segment + ".java";
			}
			path = path.segment(segment);
		}
		return path;
	}

	@Override
	public PossibleData storeJava(final String code)
	{
		try
		{
			final CompilationUnit compilationUnit = JavaParser.parse(code);

			final Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();

			if (!packageDeclaration.isPresent())
			{
				LOGGER.warn("Java code has no package declaration");
				return PossibleData.requestFailedSchema();
			}

			final NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
			if (types == null || types.size() != 1)
			{
				LOGGER.warn(
						"Java code does not contain a unique type declaration. We need this to determine where the file gets put");
				return PossibleData.requestFailedSchema();
			}

			final String typeName = packageDeclaration.get().getPackageName() + "." + types.get(0).getNameAsString();
			final FsPath filePath = pathToType(typeName);

			final FilesystemSnapshot snapshot = filesystem.snapshot();

			if (snapshot.isFile(filePath))
			{
				return PossibleData.alreadyExists();
			}

			snapshot.orderedWrite(filePath.leadingPortions(), (path, file) -> {
				if (path.equals(filePath))
				{
					FileUtils.write(file, code, StandardCharsets.UTF_8);
				}
				else
				{
					file.mkdir();
				}
			});
			return PossibleData.of(typeName);
		}
		catch (final ParseProblemException e)
		{
			LOGGER.catching(e);
			return PossibleData.requestHasInvalidSyntax();
		}
	}

	@Override
	public PossibleData getJava(final String qualifiedTypeName)
	{
		final FsPath filePath = pathToType(qualifiedTypeName);
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		if (snapshot.isFile(filePath))
		{
			final String data = snapshot.read(filePath,
					inputStream -> IOUtils.toString(inputStream, StandardCharsets.UTF_8));
			return PossibleData.of(data);
		}
		else
		{
			return PossibleData.doesNotExist();
		}
	}
}
