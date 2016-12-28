package restless.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableList;

import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.nginx.parser.NginxDirective;
import restless.handler.nginx.parser.NginxRoot;
import restless.handler.nginx.parser.NginxSyntax;
import restless.system.config.RestlessConfig;

final class ConfigHelperImpl implements ConfigHelper
{
	private final RestlessConfig config;
	private final FilesystemStore filesystem;

	// State
	private final FilesystemSnapshot snapshot;
	private final NginxRoot root;

	@Inject
	private ConfigHelperImpl(
			final RestlessConfig config,
			final FilesystemStore filesystem,
			final NginxSyntax syntax)
	{
		this.config = checkNotNull(config);
		this.filesystem = checkNotNull(filesystem);

		this.snapshot = filesystem.snapshot();

		final String text = snapshot.read(path(),
				in -> IOUtils.toString(in, StandardCharsets.UTF_8));
		this.root = syntax.parse(text);
	}

	private FsPath path()
	{
		return filesystem.systemBucket().segment("nginx.conf");
	}

	@Override
	public String absolutePath()
	{
		return path().in(config.dataDir()).getAbsolutePath();
	}

	private void newConfig()
	{
		root.contents().getOrCreateSimple("pid").setSingleParameter(sysFilePath("nginx.pid"));
		root.contents().getOrCreateSimple("error_log").setSingleParameter(sysFilePath("nginx-error.log"));

		final NginxDirective http = root.contents().getOrCreateBlock("http");
		http.contents().getOrCreateSimple("access_log").setSingleParameter(sysFilePath("nginx-access.log"));
		http.contents().getOrCreateSimple("charset").setSingleParameter(sysFilePath("utf-8"));
		final NginxDirective server = http.contents().addBlock("server", ImmutableList.of());
		server.contents().getOrCreateSimple("listen").setSingleParameter(local(config.mainPort()));
	}

	private String local(final int port)
	{
		return "127.0.0.1:" + port;
	}

	private String sysFilePath(final String relativePath)
	{
		return filesystem
				.systemBucket()
				.slashSeparatedSegments(relativePath)
				.in(config.dataDir())
				.getAbsolutePath();
	}

	@Override
	public void write()
	{
		final String text = root.toString();
		snapshot.writeSingleText(path(), text);
	}

	@Override
	public Map<Integer, ConfigHelperServer> servers()
	{
		return serverStream()
				.collect(Collectors.toMap(s -> s.port().get(), s -> s));
	}

	@Override
	public List<ConfigHelperServer> serverList()
	{
		return serverStream()
				.collect(Collectors.toList());
	}

	private Stream<ConfigHelperServer> serverStream()
	{
		return root.contents()
				.getOrCreateBlock("http")
				.contents().getAll("server")
				.stream()
				.map(ConfigHelperServerImpl::of);
	}

}
