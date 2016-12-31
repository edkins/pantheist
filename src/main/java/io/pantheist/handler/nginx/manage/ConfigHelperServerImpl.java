package io.pantheist.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import io.pantheist.handler.nginx.parser.NginxDirective;

final class ConfigHelperServerImpl implements ConfigHelperServer
{
	private final NginxDirective directive;

	private ConfigHelperServerImpl(final NginxDirective directive)
	{
		this.directive = checkNotNull(directive);
	}

	static ConfigHelperServer of(final NginxDirective directive)
	{
		return new ConfigHelperServerImpl(directive);
	}

	private int parsePort(final String value)
	{
		return Integer.parseInt(value.substring(value.indexOf(':') + 1));
	}

	@Override
	public Optional<Integer> port()
	{
		return directive.contents()
				.lookup("listen")
				.filter(value -> value.contains(":"))
				.map(value -> parsePort(value));
	}

	@Override
	public Map<String, ConfigHelperLocation> locations()
	{
		return locationStream()
				.collect(Collectors.toMap(l -> l.location(), l -> l));
	}

	@Override
	public List<ConfigHelperLocation> locationList()
	{
		return locationStream().collect(Collectors.toList());
	}

	private Stream<ConfigHelperLocation> locationStream()
	{
		return directive
				.contents()
				.getAll("location")
				.stream()
				.map(ConfigHelperLocationImpl::of);
	}

	@Override
	public NginxDirective directive()
	{
		return directive;
	}

	@Override
	public ConfigHelperLocation getOrCreateLocation(final String location)
	{
		final ConfigHelperLocation loc = locations().get(location);
		if (loc == null)
		{
			return ConfigHelperLocationImpl.of(directive.contents().addBlock("location", ImmutableList.of(location)));
		}
		else
		{
			return loc;
		}
	}

	@Override
	public boolean removeLocation(final String location)
	{
		return directive.contents().removeIf(d -> ConfigHelperLocationImpl.of(d).location().equals(location));
	}
}
