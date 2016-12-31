package restless.api.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import restless.api.java.model.ApiJavaBinding;
import restless.api.java.model.ApiJavaFile;
import restless.api.java.model.ApiJavaModelFactory;
import restless.api.java.model.ListFileResponse;
import restless.api.java.model.ListJavaPkgItem;
import restless.api.java.model.ListJavaPkgResponse;
import restless.common.api.model.CommonApiModelFactory;
import restless.common.api.model.ListClassifierResponse;
import restless.common.api.url.UrlTranslation;
import restless.common.util.AntiIt;
import restless.common.util.FailureReason;
import restless.common.util.OtherPreconditions;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.model.Entity;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaBinding;
import restless.handler.java.model.JavaFileId;
import restless.handler.java.model.JavaModelFactory;
import restless.handler.kind.backend.KindValidation;

final class JavaBackendImpl implements JavaBackend
{
	private final JavaStore javaStore;
	private final ApiJavaModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final JavaModelFactory javaFactory;
	private final CommonApiModelFactory commonFactory;
	private final KindValidation kindValidation;

	@Inject
	JavaBackendImpl(
			final JavaStore javaStore,
			final ApiJavaModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final JavaModelFactory javaFactory,
			final CommonApiModelFactory commonFactory,
			final KindValidation kindValidation)
	{
		this.javaStore = checkNotNull(javaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.javaFactory = checkNotNull(javaFactory);
		this.commonFactory = checkNotNull(commonFactory);
		this.kindValidation = checkNotNull(kindValidation);
	}

	@Override
	public Possible<Void> putJavaFile(final String pkg, final String file, final String code)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		return javaStore.putJava(id, code, false);
	}

	@Override
	public Possible<String> getJavaFile(final String pkg, final String file)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		return javaStore.getJava(id);
	}

	private ListJavaPkgResponse toListJavaPkgResponse(final List<ListJavaPkgItem> childResources)
	{
		return modelFactory.listJavaPkgResponse(
				childResources,
				urlTranslation.javaPkgCreateAction(),
				urlTranslation.javaPkgBindingAction());
	}

	@Override
	public ListJavaPkgResponse listJavaPackages()
	{
		final Set<String> packages = new HashSet<>();
		javaStore.allJavaFiles()
				.forEach(javaFileId -> packages.add(javaFileId.pkg()));

		return AntiIt.from(packages)
				.map(pkg -> modelFactory.listJavaPkgItem(urlTranslation.javaPkgToUrl(pkg)))
				.wrap(this::toListJavaPkgResponse);
	}

	@Override
	public Possible<ListClassifierResponse> listJavaPackageClassifiers(final String pkg)
	{
		if (javaStore.packageExists(pkg))
		{
			return View.ok(commonFactory.listClassifierResponse(urlTranslation.listJavaPkgClassifiers(pkg)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<ListFileResponse> listFilesInPackage(final String pkg)
	{
		if (javaStore.packageExists(pkg))
		{
			return javaStore.filesInPackage(pkg)
					.map(urlTranslation::javaToUrl)
					.map(modelFactory::listFileItem)
					.wrap(xs -> View.ok(modelFactory.listFileResponse(xs)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private String kindUrlFromEntity(final Entity entity)
	{
		OtherPreconditions.checkNotNullOrEmpty(entity.kindId());
		return urlTranslation.kindToUrl(entity.kindId());
	}

	@Override
	public Possible<ApiJavaFile> describeJavaFile(final String pkg, final String file)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		if (javaStore.fileExists(id))
		{
			final Optional<Entity> entity = kindValidation.discoverJavaKind(id);
			return View.ok(modelFactory.javaFile(
					urlTranslation.javaFileDataAction(id),
					urlTranslation.javaFileDeleteAction(id),
					entity.map(this::kindUrlFromEntity).orElse(null)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> deleteJavaFile(final String pkg, final String file)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		if (javaStore.deleteFile(id))
		{
			return View.noContent();
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> putJavaBinding(final ApiJavaBinding request)
	{
		javaStore.setJavaBinding(fromApiJavaBinding(request));
		return View.noContent();
	}

	private JavaBinding fromApiJavaBinding(final ApiJavaBinding request)
	{
		return javaFactory.javaBinding(request.location());
	}

	@Override
	public ApiJavaBinding getJavaBinding()
	{
		return toApiJavaBinding(javaStore.getJavaBinding());
	}

	private ApiJavaBinding toApiJavaBinding(final JavaBinding javaBinding)
	{
		return modelFactory.javaBinding(javaBinding.location());
	}

}
