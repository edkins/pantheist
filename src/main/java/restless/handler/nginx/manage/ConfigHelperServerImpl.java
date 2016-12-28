package restless.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import restless.common.util.MutableByKey;
import restless.common.util.OptView;
import restless.handler.nginx.parser.NginxDirective;

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
	public OptView<Integer> port()
	{
		return directive.contents()
				.lookup("listen")
				.filter(value -> value.contains(":"))
				.map(value -> parsePort(value));
	}

	@Override
	public MutableByKey<String, ConfigHelperLocation> locations()
	{
		return directive
				.contents()
				.byName()
				.getAll("location")
				.translate(ConfigHelperLocationImpl::of, ConfigHelperLocation::directive)
				.organizeByKey(ConfigHelperLocation::location);
	}

	@Override
	public NginxDirective directive()
	{
		return directive;
	}

	@Override
	public ConfigHelperLocation getOrCreateLocation(final String location)
	{
		final OptView<ConfigHelperLocation> loc = locations().optGet(location);
		if (loc.isPresent())
		{
			return loc.get();
		}
		else
		{
			final ConfigHelperLocation loc2 = ConfigHelperLocationImpl.of(directive.contents().addBlock("location"));
			loc2.directive().parameters().setSingle(location);
			return loc2;
		}
	}
}
