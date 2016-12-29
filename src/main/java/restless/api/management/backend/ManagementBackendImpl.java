package restless.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import restless.api.management.model.ApiComponent;
import restless.api.management.model.ApiEntity;
import restless.api.management.model.ApiManagementModelFactory;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListComponentItem;
import restless.api.management.model.ListComponentResponse;
import restless.api.management.model.ListConfigItem;
import restless.api.management.model.ListConfigResponse;
import restless.common.util.Escapers;
import restless.common.util.FailureReason;
import restless.common.util.OtherCollectors;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.Entity;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaComponent;
import restless.handler.kind.backend.KindStore;
import restless.handler.kind.model.Kind;
import restless.handler.nginx.manage.NginxService;
import restless.handler.schema.backend.JsonSchemaStore;
import restless.handler.schema.model.SchemaComponent;
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
	private final EntityStore entityStore;
	private final UrlTranslation urlTranslation;
	private final EntityModelFactory entityFactory;
	private final KindStore kindStore;

	@Inject
	ManagementBackendImpl(
			final FilesystemStore filesystem,
			final JavaStore javaStore,
			final RestlessConfig config,
			final ApiManagementModelFactory modelFactory,
			final NginxService nginxService,
			final JsonSchemaStore schemaStore,
			final EntityStore entityStore,
			final UrlTranslation urlTranslation,
			final EntityModelFactory entityFactory,
			final KindStore kindStore)
	{
		this.filesystem = checkNotNull(filesystem);
		this.javaStore = checkNotNull(javaStore);
		this.config = checkNotNull(config);
		this.modelFactory = checkNotNull(modelFactory);
		this.nginxService = checkNotNull(nginxService);
		this.schemaStore = checkNotNull(schemaStore);
		this.entityStore = checkNotNull(entityStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.entityFactory = checkNotNull(entityFactory);
		this.kindStore = checkNotNull(kindStore);
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

	@Nullable
	private <T, U> U nullable(final Function<T, U> fn, @Nullable final T x)
	{
		if (x == null)
		{
			return null;
		}
		else
		{
			return fn.apply(x);
		}
	}

	@Nullable
	private <T, U, V> V nullable2(final BiFunction<T, U, V> fn, @Nullable final T x, @Nullable final U y)
	{
		if (x == null && y == null)
		{
			return null;
		}
		else if (x != null && y != null)
		{
			return fn.apply(x, y);
		}
		else
		{
			throw new NullPointerException("Either both must be null, or neither");
		}
	}

	private Entity fromApiEntity(final ApiEntity entity)
	{
		return entityFactory.entity(
				nullable(urlTranslation::kindFromUrl, entity.kindUrl()),
				nullable(urlTranslation::jsonSchemaFromUrl, entity.jsonSchemaUrl()),
				nullable(urlTranslation::javaPkgFromUrl, entity.javaUrl()),
				nullable(urlTranslation::javaFileFromUrl, entity.javaUrl()));
	}

	private ApiEntity toApiEntity(final Entity entity)
	{
		final boolean valid = validateEntityAgainstKind(entity);
		return modelFactory.entity(
				nullable(urlTranslation::kindToUrl, entity.kindId()),
				nullable(urlTranslation::jsonSchemaToUrl, entity.jsonSchemaId()),
				nullable2(urlTranslation::javaToUrl, entity.javaPkg(), entity.javaFile()),
				valid);
	}

	@Override
	public Possible<Void> putEntity(final String entityId, final ApiEntity entity)
	{
		// We don't validate against kind here. It's ok to put invalid things.
		return entityStore.putEntity(entityId, fromApiEntity(entity));
	}

	@Override
	public Possible<ApiEntity> getEntity(final String entityId)
	{
		return entityStore.getEntity(entityId).map(this::toApiEntity);
	}

	@Override
	public Possible<ApiComponent> getComponent(final String entityId, final String componentId)
	{
		return entityStore.getEntity(entityId).posMap(e -> {
			SchemaComponent jsonSchema = null;
			JavaComponent java = null;
			if (e.jsonSchemaId() != null)
			{
				jsonSchema = schemaStore.getJsonSchemaComponent(e.jsonSchemaId(), componentId).orElse(null);
			}
			if (e.javaFile() != null)
			{
				checkNotNull(e.javaPkg());
				java = javaStore.getJavaComponent(e.javaPkg(), e.javaFile(), componentId).orElse(null);
			}

			if (jsonSchema != null || java != null)
			{
				return View.ok(modelFactory.component(jsonSchema, java));
			}
			else
			{
				// None of the handlers know anything about this component.
				return FailureReason.DOES_NOT_EXIST.happened();
			}
		});
	}

	@Override
	public Possible<ListComponentResponse> listComponents(final String entityId)
	{
		return entityStore.getEntity(entityId).map(e -> {
			final Map<String, ListComponentItem> result = new HashMap<>();

			if (e.jsonSchemaId() != null)
			{
				schemaStore.listComponents(e.jsonSchemaId()).forEach(c -> {
					result.put(c.componentId(), modelFactory.listComponentItem(c.componentId()));
				});
			}
			return result.values().stream().collect(OtherCollectors.wrapped(modelFactory::listComponentResponse));
		});
	}

	@Override
	public Possible<Kind> getKind(final String kindId)
	{
		return kindStore.getKind(kindId);
	}

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind)
	{
		return kindStore.putKind(kindId, kind);
	}

	private boolean validateEntityAgainstKind(final Entity entity)
	{
		if (entity.kindId() == null)
		{
			// If kind is missing then anything is valid.
			return true;
		}
		final Kind kind = getKind(entity.kindId()).get();
		if (kind.java() != null)
		{
			if (kind.java().required() && (entity.javaPkg() == null || entity.javaFile() == null))
			{
				// Java is required but not present in entity.
				return false;
			}
			if (!javaStore.validateKind(entity.javaPkg(), entity.javaFile(), kind.java()))
			{
				// Java store says the details are invalid.
				return false;
			}
		}

		// Otherwise assume ok.
		return true;
	}
}
