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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import restless.common.util.AntiIterator;
import restless.common.util.FailureReason;
import restless.common.util.OtherPreconditions;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.java.model.JavaComponent;
import restless.handler.java.model.JavaFileId;
import restless.handler.java.model.JavaModelFactory;
import restless.handler.kind.model.JavaClause;
import restless.handler.kind.model.JavaKind;

final class JavaStoreImpl implements JavaStore
{
	private static final String DOT_JAVA = ".java";
	private static final Logger LOGGER = LogManager.getLogger(JavaStoreImpl.class);
	private static final String ROOT = ".";
	private final FilesystemStore filesystem;
	private final JavaModelFactory modelFactory;

	@Inject
	private JavaStoreImpl(final FilesystemStore filesystem, final JavaModelFactory modelFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.modelFactory = checkNotNull(modelFactory);
	}

	private FsPath pathToType(final String pkg, final String file)
	{
		OtherPreconditions.checkNotNullOrEmpty(pkg);
		OtherPreconditions.checkNotNullOrEmpty(file);
		if (file.contains(".") || file.contains("/"))
		{
			throw new IllegalArgumentException("Bad filename: " + file);
		}
		return packagePath(pkg).segment(file + DOT_JAVA);
	}

	private FsPath packagePath(final String pkg)
	{
		OtherPreconditions.checkNotNullOrEmpty(pkg);
		if (pkg.startsWith(".") || pkg.contains("..") || pkg.endsWith(".") || pkg.contains("/"))
		{
			throw new IllegalArgumentException("Bad pkg: " + pkg);
		}

		FsPath path = rootJavaPath();
		for (final String seg : pkg.split("\\."))
		{
			path = path.segment(seg);
		}
		return path;
	}

	private FsPath rootJavaPath()
	{
		return filesystem.systemBucket().segment("java");
	}

	@Override
	public Possible<Void> putJava(final JavaFileId javaFileId, final String code, final boolean failIfExists)
	{
		checkNotNull(javaFileId);
		checkNotNull(code);
		final String pkg = javaFileId.pkg();
		final String file = javaFileId.file();
		final CompilationUnit compilationUnit;

		if (code.isEmpty())
		{
			LOGGER.warn("Code is empty");
			return FailureReason.REQUEST_HAS_INVALID_SYNTAX.happened();
		}

		try
		{
			compilationUnit = JavaParser.parse(code);
		}
		catch (final ParseProblemException e)
		{
			LOGGER.catching(e);
			return FailureReason.REQUEST_HAS_INVALID_SYNTAX.happened();
		}

		final Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();

		if (!packageDeclaration.isPresent())
		{
			LOGGER.warn("Java code has no package declaration");
			return FailureReason.REQUEST_FAILED_SCHEMA.happened();
		}

		final NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
		if (types == null || types.size() != 1)
		{
			LOGGER.warn(
					"Java code does not contain a unique type declaration. We need this to determine where the file gets put");
			return FailureReason.REQUEST_FAILED_SCHEMA.happened();
		}

		if (!pkg.equals(packageDeclaration.get().getPackageName()))
		{
			LOGGER.warn("Wrong package. Request said " + pkg + ", java code said " + packageDeclaration.get());
			return FailureReason.REQUEST_FAILED_SCHEMA.happened();
		}
		if (!file.equals(types.get(0).getNameAsString()))
		{
			LOGGER.warn(
					"Wrong type name. Request said " + file + ", java code said " + types.get(0).getNameAsString());
			return FailureReason.REQUEST_FAILED_SCHEMA.happened();
		}
		final FsPath filePath = pathToType(pkg, file);

		final FilesystemSnapshot snapshot = filesystem.snapshot();

		if (snapshot.isFile(filePath)) // still need to call isFile even if we don't care
		{
			if (failIfExists)
			{
				return FailureReason.ALREADY_EXISTS.happened();
			}
		}

		filePath.parent().leadingPortions().forEach(dirPath -> snapshot.isDir(dirPath));

		snapshot.write(map -> {
			for (final FsPath dirPath : filePath.parent().leadingPortions())
			{
				map.get(dirPath).mkdir();
			}
			FileUtils.write(map.get(filePath), code, StandardCharsets.UTF_8);
		});
		return View.noContent();
	}

	@Override
	public Possible<String> getJava(final JavaFileId javaFileId)
	{
		checkNotNull(javaFileId);
		final FsPath filePath = idToPath(javaFileId);
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		if (snapshot.isFile(filePath))
		{
			final String data = snapshot.read(filePath,
					inputStream -> IOUtils.toString(inputStream, StandardCharsets.UTF_8));
			return View.ok(data);
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private FsPath idToPath(final JavaFileId javaFileId)
	{
		return pathToType(javaFileId.pkg(), javaFileId.file());
	}

	@Override
	public Optional<JavaComponent> getJavaComponent(final JavaFileId javaFileId, final String componentId)
	{
		checkNotNull(javaFileId);
		if (componentId.equals(ROOT))
		{
			return Optional.of(modelFactory.component(true));
		}
		else
		{
			return Optional.empty();
		}
	}

	/**
	 * Return what we think this is, or empty if we have no idea.
	 */
	private Optional<JavaKind> classify(final TypeDeclaration<?> type)
	{
		if (type instanceof ClassOrInterfaceDeclaration)
		{
			if (((ClassOrInterfaceDeclaration) type).isInterface())
			{
				return Optional.of(JavaKind.INTERFACE);
			}
			else
			{
				return Optional.of(JavaKind.CLASS);
			}
		}
		else if (type instanceof EnumDeclaration)
		{
			return Optional.of(JavaKind.ENUM);
		}
		else
		{
			// possibly annotation?
			return Optional.empty();
		}
	}

	@Override
	public boolean validateKind(final JavaFileId javaFileId, final JavaClause javaClause)
	{
		checkNotNull(javaFileId);
		checkNotNull(javaClause);
		final String code = getJava(javaFileId).get();
		final CompilationUnit compilationUnit = JavaParser.parse(code);

		if (javaClause.javaKind() != null)
		{
			if (compilationUnit.getTypes() == null || compilationUnit.getTypes().size() != 1)
			{
				// can only compare java kind when there's exactly one type defined.
				return false;
			}

			// Work out the actual java kind and see if it agrees.
			final TypeDeclaration<?> type = compilationUnit.getType(0);
			final Optional<JavaKind> javaKind = classify(type);
			if (!javaKind.isPresent() || !javaClause.javaKind().encompasses(javaKind))
			{
				return false;
			}
		}

		// Nothing left to complain about.
		return true;
	}

	private String withoutDotJava(final String segment)
	{
		if (segment.endsWith(DOT_JAVA))
		{
			final String result = segment.substring(0, segment.length() - DOT_JAVA.length());
			if (result.isEmpty())
			{
				throw new IllegalArgumentException("Can't have a file just called .java!");
			}
			return result;
		}
		else
		{
			throw new IllegalArgumentException("Does not end in .java");
		}
	}

	private JavaFileId fileIdFromPath(final FsPath path)
	{
		final String file = withoutDotJava(path.lastSegment());
		final String pkg = path.segmentsRelativeTo(rootJavaPath()).init().join(".").get();
		return modelFactory.fileId(pkg, file);
	}

	@Override
	public Optional<JavaFileId> findFileByName(final String fileName)
	{
		final String fileNameDotJava = fileName + DOT_JAVA;
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.recurse(rootJavaPath())
				.filter(snapshot::safeIsFile)
				.filter(path -> path.lastSegment().equals(fileNameDotJava))
				.failIfMultiple()
				.map(this::fileIdFromPath);
	}

	@Override
	public AntiIterator<JavaFileId> allJavaFiles()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.recurse(rootJavaPath())
				.filter(snapshot::safeIsFile)
				.filter(path -> path.lastSegment().endsWith(DOT_JAVA))
				.map(this::fileIdFromPath);
	}

	@Override
	public boolean packageExists(final String pkg)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.listFilesAndDirectories(packagePath(pkg))
				.filter(snapshot::safeIsFile)
				.filter(path -> path.lastSegment().endsWith(DOT_JAVA))
				.foundAny();
	}

	@Override
	public AntiIterator<JavaFileId> filesInPackage(final String pkg)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.listFilesAndDirectories(packagePath(pkg))
				.filter(snapshot::safeIsFile)
				.filter(path -> path.lastSegment().endsWith(DOT_JAVA))
				.map(this::fileIdFromPath);
	}

	@Override
	public boolean fileExists(final JavaFileId fileId)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot.isFile(idToPath(fileId));
	}

	@Override
	public boolean deleteFile(final JavaFileId fileId)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = idToPath(fileId);
		if (snapshot.isFile(path))
		{
			snapshot.writeSingle(path, file -> file.delete());
			return true;
		}
		else
		{
			return false;
		}
	}
}
