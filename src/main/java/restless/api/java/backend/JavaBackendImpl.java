package restless.api.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import restless.api.management.model.ApiManagementModelFactory;
import restless.api.management.model.ListClassifierResponse;
import restless.api.management.model.ListJavaPkgResponse;
import restless.common.util.AntiIt;
import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaFileId;
import restless.handler.java.model.JavaModelFactory;
import restless.handler.kind.backend.KindStore;
import restless.handler.uri.UrlTranslation;

final class JavaBackendImpl implements JavaBackend
{
	private final JavaStore javaStore;
	private final ApiManagementModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final JavaModelFactory javaFactory;

	@Inject
	JavaBackendImpl(
			final JavaStore javaStore,
			final ApiManagementModelFactory modelFactory,
			final EntityStore entityStore,
			final UrlTranslation urlTranslation,
			final EntityModelFactory entityFactory,
			final KindStore kindStore,
			final JavaModelFactory javaFactory)
	{
		this.javaStore = checkNotNull(javaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.javaFactory = checkNotNull(javaFactory);
	}

	@Override
	public Possible<Void> putJavaFile(final String pkg, final String file, final String code)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		return javaStore.putJava(id, code);
	}

	@Override
	public Possible<String> getJavaFile(final String pkg, final String file)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		return javaStore.getJava(id);
	}

	@Override
	public ListJavaPkgResponse listJavaPackages()
	{
		final Set<String> packages = new HashSet<>();
		javaStore.allJavaFiles()
				.forEach(javaFileId -> packages.add(javaFileId.pkg()));

		return AntiIt.from(packages)
				.map(pkg -> modelFactory.listJavaPkgItem(urlTranslation.javaPkgToUrl(pkg)))
				.wrap(modelFactory::listJavaPkgResponse);
	}

	@Override
	public Possible<ListClassifierResponse> listJavaPackageClassifiers(final String pkg)
	{
		if (javaStore.packageExists(pkg))
		{
			return View.ok(modelFactory.listClassifierResponse(urlTranslation.listJavaPkgClassifiers(pkg)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

}
