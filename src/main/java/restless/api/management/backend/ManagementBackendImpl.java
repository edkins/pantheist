package restless.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import restless.api.management.model.ApiManagementModelFactory;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListConfigItem;
import restless.api.management.model.ListConfigResponse;
import restless.glue.nginx.filesystem.NginxFilesystemGlue;
import restless.handler.binding.backend.BindingStore;
import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;
import restless.handler.binding.backend.SchemaValidation;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingMatch;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.ConfigId;
import restless.handler.binding.model.Handler;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.PathSpecSegment;
import restless.handler.binding.model.Schema;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.java.backend.JavaStore;
import restless.system.config.RestlessConfig;

final class ManagementBackendImpl implements ManagementBackend
{
	private final BindingModelFactory bindingFactory;
	private final BindingStore bindingStore;
	private final FilesystemStore filesystem;
	private final NginxFilesystemGlue nginxFilesystemGlue;
	private final SchemaValidation schemaValidation;
	private final JavaStore javaStore;
	private final RestlessConfig config;
	private final ApiManagementModelFactory modelFactory;

	@Inject
	ManagementBackendImpl(final BindingModelFactory bindingFactory,
			final BindingStore bindingStore,
			final FilesystemStore filesystem,
			final NginxFilesystemGlue nginxFilesystemGlue,
			final SchemaValidation schemaValidation,
			final JavaStore javaStore,
			final RestlessConfig config,
			final ApiManagementModelFactory modelFactory)
	{
		this.bindingFactory = checkNotNull(bindingFactory);
		this.bindingStore = checkNotNull(bindingStore);
		this.filesystem = checkNotNull(filesystem);
		this.nginxFilesystemGlue = checkNotNull(nginxFilesystemGlue);
		this.schemaValidation = checkNotNull(schemaValidation);
		this.javaStore = checkNotNull(javaStore);
		this.config = checkNotNull(config);
		this.modelFactory = checkNotNull(modelFactory);
	}

	private Binding changeHandler(final Handler handler, final Binding b)
	{
		return bindingFactory.binding(
				b.pathSpec(),
				handler,
				b.schema(),
				b.jerseyClass(),
				b.configId());
	}

	@Override
	public PossibleEmpty putConfig(final ConfigId pathSpec, final Handler handler)
	{
		bindingStore.changeConfig(pathSpec, b -> changeHandler(handler, b));
		restartServers();
		return PossibleEmpty.ok();
	}

	@Override
	public PossibleEmpty putData(final PathSpec path, final String data)
	{
		final Optional<BindingMatch> match = bindingStore.lookup(path);
		if (match.isPresent())
		{
			return schemaValidation
					.validate(match.get().binding().schema(), data)
					.then(() -> functionsFor(match.get(), path).putString(data));
		}
		else
		{
			return PossibleEmpty.doesNotExist();
		}
	}

	@Override
	public PossibleData getData(final PathSpec path)
	{
		final Optional<BindingMatch> match = bindingStore.lookup(path);
		if (match.isPresent())
		{
			return functionsFor(match.get(), path).getString();
		}
		else
		{
			return PossibleData.doesNotExist();
		}
	}

	@Override
	public PossibleEmpty putJsonSchema(final ConfigId pathSpec, final JsonNode jsonNode)
	{
		final Schema schema = bindingFactory.jsonSchema(jsonNode);
		return schemaValidation.checkSchema(schema).then(() -> {
			bindingStore.changeConfig(pathSpec, b -> changeSchema(schema, b));
		});
	}

	private Binding changeSchema(final Schema schema, final Binding b)
	{
		return bindingFactory.binding(b.pathSpec(), b.handler(), schema, b.jerseyClass(), b.configId());
	}

	private ManagementFunctions functionsFor(final BindingMatch match, final PathSpec path)
	{
		final Binding binding = match.binding();
		switch (binding.handler().type()) {
		case filesystem:
			return filesystem.manage(filesystem.srvBucket().withPathSegments(path.segments()));
		default:
			throw new UnsupportedOperationException("Unrecognized handler: " + binding.handler());
		}
	}

	private void restartServers()
	{
		nginxFilesystemGlue.startStopOrRestart();
	}

	@Override
	public Schema getSchema(final ConfigId pathSpec)
	{
		return bindingStore.exact(pathSpec).schema();
	}

	@Override
	public PossibleEmpty putJerseyFile(final ConfigId pathSpec, final String code)
	{
		return javaStore.storeJava(code).thenEmpty(className -> {
			bindingStore.changeConfig(pathSpec, b -> changeJerseyClass(className, b));
			return PossibleEmpty.ok();
		});
	}

	private Binding changeJerseyClass(final String jerseyClass, final Binding b)
	{
		return bindingFactory.binding(b.pathSpec(), b.handler(), b.schema(), jerseyClass, b.configId());
	}

	@Override
	public PossibleData getJerseyFile(final ConfigId pathSpec)
	{
		final String jerseyClass = bindingStore.exact(pathSpec).jerseyClass();
		if (jerseyClass == null)
		{
			return PossibleData.doesNotExist();
		}
		else
		{
			return javaStore.getJava(jerseyClass);
		}
	}

	@Override
	public PathSpec literalPath(final String path)
	{
		final ImmutableList.Builder<PathSpecSegment> builder = ImmutableList.builder();
		if (!path.isEmpty())
		{
			for (final String seg : path.split("\\/"))
			{
				builder.add(bindingFactory.literal(seg));
			}
		}
		return bindingFactory.pathSpec(builder.build());
	}

	@Override
	public URI createConfig(final CreateConfigRequest request)
	{
		final PathSpec pathSpec = literalPath(request.pathSpec()).plus(bindingFactory.multi());
		final ConfigId configId = bindingStore.createConfig(pathSpec);
		return urlForConfigId(configId);
	}

	private URI urlForConfigId(final ConfigId configId)
	{
		return managementUriBuilder().path("config").path(configId.toString()).build();
	}

	private UriBuilder managementUriBuilder()
	{
		return UriBuilder.fromUri("http://localhost").port(config.managementPort());
	}

	@Override
	public boolean configExists(final ConfigId configId)
	{
		return bindingStore.has(configId);
	}

	@Override
	public PossibleEmpty deleteConfig(final ConfigId configId)
	{
		if (bindingStore.deleteConfig(configId))
		{
			return PossibleEmpty.ok();
		}
		else
		{
			return PossibleEmpty.doesNotExist();
		}
	}

	private ListConfigItem makeListConfigItem(final Binding binding)
	{
		return modelFactory.listConfigItem(urlForConfigId(binding.configId()).toString());
	}

	@Override
	public ListConfigResponse listConfig()
	{
		final List<ListConfigItem> list = Lists.transform(bindingStore.listBindings(), this::makeListConfigItem);
		return modelFactory.listConfigResponse(list);
	}

}
