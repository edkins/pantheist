package io.pantheist.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ImmutableList;

import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.nginx.parser.NginxBlock;
import io.pantheist.handler.nginx.parser.NginxDirective;
import io.pantheist.handler.nginx.parser.NginxSyntax;
import io.pantheist.system.config.PantheistConfig;

final class ConfigHelperImpl implements ConfigHelper
{
	private final PantheistConfig config;
	private final FilesystemStore filesystem;

	// State
	private final FilesystemSnapshot snapshot;
	private final NginxBlock root;

	@Inject
	private ConfigHelperImpl(
			final PantheistConfig config,
			final FilesystemStore filesystem,
			final NginxSyntax syntax)
	{
		this.config = checkNotNull(config);
		this.filesystem = checkNotNull(filesystem);

		this.snapshot = filesystem.snapshot();

		final String text;
		if (snapshot.isFile(path()))
		{
			text = snapshot.read(path(),
					in -> IOUtils.toString(in, StandardCharsets.UTF_8));
		}
		else
		{
			text = "";
		}
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
		return root
				.getOrCreateBlock("http")
				.contents().getAll("server")
				.stream()
				.map(ConfigHelperServerImpl::of);
	}

	@Override
	public boolean isEmpty()
	{
		return root.isEmpty();
	}

	@Override
	public ConfigHelperServer createLocalServer(final int port)
	{
		final NginxDirective serverDirective = root
				.getOrCreateBlock("http")
				.contents()
				.addBlock("server", ImmutableList.of());
		serverDirective.contents().getOrCreateSimple("listen").setSingleParameter("127.0.0.1:" + port);
		return ConfigHelperServerImpl.of(serverDirective);
	}

	@Override
	public void set(final String key, final String value)
	{
		root.getOrCreateSimple(key).setSingleParameter(value);
	}

	@Override
	public void setType(final String mimeType, final String extension)
	{
		root.getOrCreateBlock("http").contents()
				.getOrCreateBlock("types").contents()
				.getOrCreateSimple(mimeType).setSingleParameter(extension);
	}

	@Override
	public void setHttp(final String key, final String value)
	{
		root.getOrCreateBlock("http").contents().getOrCreateSimple(key).setSingleParameter(value);
	}

	@Override
	public void createEventsSection()
	{
		root.getOrCreateBlock("events");
	}

}
