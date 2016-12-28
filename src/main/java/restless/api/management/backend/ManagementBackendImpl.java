package restless.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.api.management.model.ApiManagementModelFactory;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListConfigItem;
import restless.api.management.model.ListConfigResponse;
import restless.common.util.Escapers;
import restless.common.util.FailureReason;
import restless.common.util.ListView;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.java.backend.JavaStore;
import restless.handler.nginx.manage.NginxService;
import restless.handler.schema.backend.JsonSchemaStore;
import restless.system.config.RestlessConfig;

final class ManagementBackendImpl implements ManagementBackend
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementBackendImpl.class);
	private final FilesystemStore filesystem;
	private final JavaStore javaStore;
	private final RestlessConfig config;
	private final ApiManagementModelFactory modelFactory;
	private final NginxService nginxService;
	private final JsonSchemaStore schemaStore;

	@Inject
	ManagementBackendImpl(
			final FilesystemStore filesystem,
			final JavaStore javaStore,
			final RestlessConfig config,
			final ApiManagementModelFactory modelFactory,
			final NginxService nginxService,
			final JsonSchemaStore schemaStore)
	{
		this.filesystem = checkNotNull(filesystem);
		this.javaStore = checkNotNull(javaStore);
		this.config = checkNotNull(config);
		this.modelFactory = checkNotNull(modelFactory);
		this.nginxService = checkNotNull(nginxService);
		this.schemaStore = checkNotNull(schemaStore);
	}

	@Override
	public Possible<Void> putConfig(final String serverId, final String locationId, final CreateConfigRequest request)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.putAndRestart(port, locationId, View.nullable(request.alias()));
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
		return javaStore.putJava(pkg, file, code);
	}

	@Override
	public Possible<String> getJavaFile(final String pkg, final String file)
	{
		return javaStore.getJava(pkg, file);
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

	private String urlForLocation(final String serverId, final String location)
	{
		return UriBuilder.fromUri("http://localhost:" + config.managementPort())
				.path("server")
				.path(serverId)
				.path("location")
				.path(Escapers.url(location))
				.build().toString();
	}

	private ListConfigItem makeListConfigItem(final String serverId, final String location)
	{
		return modelFactory.listConfigItem(urlForLocation(serverId, location).toString());
	}

	@Override
	public Possible<ListConfigResponse> listLocations(final String serverId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			final Possible<ListView<String>> locations = nginxService.listLocations(port);
			if (!locations.isPresent())
			{
				return locations.coerce();
			}
			final List<ListConfigItem> list = locations.get()
					.map(loc -> makeListConfigItem(serverId, loc))
					.toList();
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

}
