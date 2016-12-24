package restless.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;

import restless.api.management.model.ConfigRequest;
import restless.glue.nginx.filesystem.NginxFilesystemGlue;
import restless.handler.binding.backend.BindingStore;
import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingMatch;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.PathSpec;
import restless.handler.binding.model.PathSpecMatch;
import restless.handler.binding.model.PathSpecSegment;
import restless.handler.binding.model.Schema;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.nginx.manage.NginxService;

final class ManagementBackendImpl implements ManagementBackend
{
	private final BindingModelFactory bindingFactory;
	private final BindingStore bindingStore;
	private final FilesystemStore filesystem;
	private final NginxService nginxService;
	private final NginxFilesystemGlue nginxFilesystemGlue;

	@Inject
	ManagementBackendImpl(final BindingModelFactory bindingFactory,
			final BindingStore bindingStore,
			final FilesystemStore filesystem,
			final NginxService nginxService,
			final NginxFilesystemGlue nginxFilesystemGlue)
	{
		this.bindingFactory = checkNotNull(bindingFactory);
		this.bindingStore = checkNotNull(bindingStore);
		this.filesystem = checkNotNull(filesystem);
		this.nginxService = checkNotNull(nginxService);
		this.nginxFilesystemGlue = checkNotNull(nginxFilesystemGlue);
	}

	@Override
	public PathSpec pathSpec(final String path)
	{
		final ImmutableList.Builder<PathSpecSegment> builder = ImmutableList.builder();
		for (final String seg : path.split("\\/"))
		{
			builder.add(segment(seg));
		}
		return bindingFactory.pathSpec(builder.build());
	}

	private PathSpecSegment segment(final String seg)
	{
		if (seg.startsWith("+"))
		{
			return bindingFactory.literal(seg.substring(1));
		}
		else if (seg.equals("*"))
		{
			return bindingFactory.star();
		}
		else
		{
			throw new UnsupportedOperationException("Currently only literal path segments supported");
		}
	}

	private Binding changeFilesystemHandler(final PathSpec pathSpec, final FsPath bucket, final Binding b)
	{
		return bindingFactory.binding(
				pathSpec,
				bindingFactory.filesystem(bucket),
				b.schema());
	}

	@Override
	public void putConfig(final PathSpec pathSpec, final ConfigRequest config)
	{
		switch (config.handler()) {
		case filesystem:
			final FsPath bucket = filesystem.newBucket(pathSpec.nameHint());
			bindingStore.changeConfig(pathSpec, b -> changeFilesystemHandler(pathSpec, bucket, b));
			break;
		default:
			throw new UnsupportedOperationException("Unknown handler type: " + config.handler());
		}
		restartNginx();
	}

	@Override
	public void putData(final PathSpec path, final String data)
	{
		lookup(path).putString(data);
	}

	@Override
	public PossibleData getData(final PathSpec path)
	{
		return lookup(path).getString();
	}

	@Override
	public void putJsonSchema(final PathSpec pathSpec, final JsonNode schema)
	{
		bindingStore.changeConfig(pathSpec, b -> changeSchema(pathSpec, schema, b));
	}

	private Binding changeSchema(final PathSpec pathSpec, final JsonNode schema, final Binding b)
	{
		return bindingFactory.binding(pathSpec, b.handler(), bindingFactory.jsonSchema(schema));
	}

	private ManagementFunctions lookup(final PathSpec pathSpec)
	{
		final Optional<BindingMatch> match = bindingStore.lookup(pathSpec);
		if (match.isPresent())
		{
			return functionsFor(pathSpec, match.get().binding(), match.get().pathMatch());
		}
		else
		{
			throw new NotFoundException();
		}
	}

	private ManagementFunctions functionsFor(final PathSpec pathSpec,
			final Binding bindingPoint,
			final PathSpecMatch match)
	{
		switch (bindingPoint.handler().type()) {
		case filesystem:
			final FsPath path = bindingPoint.handler().filesystemBucket()
					.withPathSegments(match.nonLiteralChunk());
			return filesystem.manage(path);
		default:
			throw new UnsupportedOperationException("Unrecognized handler: " + bindingPoint.handler());
		}
	}

	private void restartNginx()
	{
		nginxService.configureAndStart(nginxFilesystemGlue.nginxConf());
	}

	@Override
	public Schema getSchema(final PathSpec pathSpec)
	{
		return bindingStore.exact(pathSpec).schema();
	}

}
