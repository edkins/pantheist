package io.pantheist.api.java.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.api.java.model.ApiJavaBinding;
import io.pantheist.api.java.model.ApiJavaModelFactory;
import io.pantheist.api.java.model.ListJavaFileItem;
import io.pantheist.api.java.model.ListJavaFileResponse;
import io.pantheist.api.java.model.ListJavaPkgItem;
import io.pantheist.api.java.model.ListJavaPkgResponse;
import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.api.model.KindedImpl;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.java.backend.JavaStore;
import io.pantheist.handler.java.model.JavaBinding;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.java.model.JavaModelFactory;
import io.pantheist.handler.kind.backend.KindValidation;

final class JavaBackendImpl implements JavaBackend
{
	private final JavaStore javaStore;
	private final ApiJavaModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final JavaModelFactory javaFactory;
	private final CommonApiModelFactory commonFactory;
	private final KindValidation kindValidation;
	private final ObjectMapper objectMapper;

	@Inject
	JavaBackendImpl(
			final JavaStore javaStore,
			final ApiJavaModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final JavaModelFactory javaFactory,
			final CommonApiModelFactory commonFactory,
			final KindValidation kindValidation,
			final ObjectMapper objectMapper)
	{
		this.javaStore = checkNotNull(javaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.javaFactory = checkNotNull(javaFactory);
		this.commonFactory = checkNotNull(commonFactory);
		this.kindValidation = checkNotNull(kindValidation);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public Possible<Void> putJavaFile(final String pkg, final String file, final String code)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		return javaStore.putJava(id, code, false);
	}

	@Override
	public Possible<Kinded<String>> getJavaFile(final String pkg, final String file)
	{
		final JavaFileId id = javaFactory.fileId(pkg, file);
		return javaStore.getJava(id).map(code -> {
			final String kindUrl = getJavaFileKindUrl(id);
			return KindedImpl.of(kindUrl, code);
		});
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

		return toListJavaPkgResponse(
				AntiIt.from(packages)
						.map(pkg -> toListJavaPkgItem(pkg))
						.toSortedList((p1, p2) -> p1.name().compareTo(p2.name())));
	}

	private ListJavaPkgItem toListJavaPkgItem(final String pkg)
	{
		return modelFactory.listJavaPkgItem(urlTranslation.javaPkgToUrl(pkg), pkg,
				urlTranslation.kindToUrl("java-package"));
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

	private ListJavaFileItem toListJavaItem(final JavaFileId id, final Map<String, ObjectNode> entityMap)
	{
		final String url = urlTranslation.javaToUrl(id);
		final String kindId;
		final ObjectNode entity = entityMap.get(id.qualifiedName());
		if (entity == null)
		{
			kindId = "java-file";
		}
		else
		{
			kindId = entity.get("kindId").textValue();
		}
		return modelFactory.listFileItem(url, id.file(), urlTranslation.kindToUrl(kindId));
	}

	@Override
	public Possible<ListJavaFileResponse> listFilesInPackage(final String pkg)
	{
		if (javaStore.packageExists(pkg))
		{
			final JsonNode pkgNode = objectMapper.getNodeFactory().textNode(pkg);
			final Map<String, ObjectNode> entityMap = kindValidation.objectsWithKind("java-file")
					.whereEqual("package", pkgNode)
					.antiIt()
					.toMap(x -> x.get("qualifiedName").textValue());

			final List<ListJavaFileItem> list = javaStore.filesInPackage(pkg)
					.map(e -> toListJavaItem(e, entityMap))
					.toSortedList((jf1, jf2) -> jf1.name().compareTo(jf2.name()));
			return View.ok(modelFactory.listFileResponse(list));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private String getJavaFileKindUrl(final JavaFileId id)
	{
		final JsonNode qnameNode = objectMapper.getNodeFactory().textNode(id.qualifiedName());
		final Optional<ObjectNode> entity = kindValidation.objectsWithKind("java-file")
				.whereEqual("qualifiedName", qnameNode)
				.antiIt()
				.failIfMultiple();
		final String kindId = entity.map(x -> x.get("kindId").textValue()).orElse("java-file");
		return urlTranslation.kindToUrl(kindId);
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

	@Override
	public Possible<String> postJava(final String code)
	{
		return javaStore.postJava(code, true).map(urlTranslation::javaToUrl);
	}

}
