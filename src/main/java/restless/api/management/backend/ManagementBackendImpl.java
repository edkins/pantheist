package restless.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import restless.api.management.model.ApiManagementModelFactory;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListClassifierResponse;
import restless.api.management.model.ListConfigItem;
import restless.api.management.model.ListConfigResponse;
import restless.api.management.model.ListJavaPkgResponse;
import restless.common.util.AntiIt;
import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaFileId;
import restless.handler.java.model.JavaModelFactory;
import restless.handler.kind.backend.KindStore;
import restless.handler.nginx.manage.NginxService;
import restless.handler.schema.backend.JsonSchemaStore;
import restless.handler.uri.UrlTranslation;

final class ManagementBackendImpl implements ManagementBackend
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementBackendImpl.class);
	private final FilesystemStore filesystem;
	private final JavaStore javaStore;
	private final ApiManagementModelFactory modelFactory;
	private final NginxService nginxService;
	private final JsonSchemaStore schemaStore;
	private final UrlTranslation urlTranslation;
	private final JavaModelFactory javaFactory;

	@Inject
	ManagementBackendImpl(
			final FilesystemStore filesystem,
			final JavaStore javaStore,
			final ApiManagementModelFactory modelFactory,
			final NginxService nginxService,
			final JsonSchemaStore schemaStore,
			final EntityStore entityStore,
			final UrlTranslation urlTranslation,
			final EntityModelFactory entityFactory,
			final KindStore kindStore,
			final JavaModelFactory javaFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.javaStore = checkNotNull(javaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.nginxService = checkNotNull(nginxService);
		this.schemaStore = checkNotNull(schemaStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.javaFactory = checkNotNull(javaFactory);
	}

	@Override
	public Possible<Void> putConfig(final String serverId, final String locationId, final CreateConfigRequest request)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.putAndRestart(port, locationId, Optional.ofNullable(request.alias()));
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> putData(final String path, final String data)
	{
		return filesystem.putSrvData(path, data);
	}

	@Override
	public Possible<String> getData(final String path)
	{
		return filesystem.getSrvData(path);
	}

	@Override
	public Possible<Void> putJsonSchema(final String schemaId, final String schemaText)
	{
		return schemaStore.putJsonSchema(schemaId, schemaText);
	}

	@Override
	public Possible<String> getJsonSchema(final String schemaId)
	{
		return schemaStore.getJsonSchema(schemaId);
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
	public boolean configExists(final String serverId, final String locationId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.hasLocation(port, locationId);
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return false;
		}
	}

	@Override
	public Possible<Void> deleteConfig(final String serverId, final String locationId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.deleteLocationAndRestart(port, locationId);
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	private ListConfigItem makeListConfigItem(final String serverId, final String location)
	{
		return modelFactory.listConfigItem(urlTranslation.locationToUrl(serverId, location).toString());
	}

	@Override
	public Possible<ListConfigResponse> listLocations(final String serverId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			final Possible<List<String>> locations = nginxService.listLocations(port);
			if (!locations.isPresent())
			{
				return locations.coerce();
			}
			final List<ListConfigItem> list = Lists.transform(locations.get(),
					loc -> makeListConfigItem(serverId, loc));
			return View.ok(modelFactory.listConfigResponse(list));
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> validateAgainstJsonSchema(final String schemaId, final String text)
	{
		return schemaStore.validateAgainstJsonSchema(schemaId, text);
	}

	@Override
	public ListClassifierResponse listRootClassifiers()
	{
		return modelFactory.listClassifierResponse(urlTranslation.listRootClassifiers());
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
