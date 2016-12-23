package restless.handler.nginx.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import restless.common.util.Make;
import restless.common.util.MutableOptional;
import restless.common.util.OtherPreconditions;

final class NginxConfigImpl implements NginxConfig
{
	private static final Pattern VALUE_PATTERN = Pattern.compile("[a-zA-Z0-9-_/.:]+");
	private final NginxVar pid;
	private final NginxVar error_log;
	private final NginxEvents events;
	private final NginxHttp http;

	@Inject
	NginxConfigImpl()
	{
		pid = new NginxVarImpl("pid", 0);
		error_log = new NginxVarImpl("error_log", 0);
		events = new NginxEventsImpl();
		http = new NginxHttpImpl();
	}

	@Override
	public NginxVar pid()
	{
		return pid;
	}

	@Override
	public NginxVar error_log()
	{
		return error_log;
	}

	@Override
	public NginxEvents events()
	{
		return events;
	}

	@Override
	public NginxHttp http()
	{
		return http;
	}

	@Override
	public String toString()
	{
		return new StringBuilder()
				.append(pid)
				.append(error_log)
				.append(events)
				.append(http)
				.toString();
	}

	private static final class NginxHttpImpl implements NginxHttp
	{
		private final NginxVar access_log;
		private final NginxVar root;
		private final List<NginxServer> servers;

		private NginxHttpImpl()
		{
			access_log = new NginxVarImpl("access_log", 1);
			root = new NginxVarImpl("root", 1);
			servers = new ArrayList<>();
		}

		@Override
		public NginxVar access_log()
		{
			return access_log;
		}

		@Override
		public NginxVar root()
		{
			return root;
		}

		@Override
		public List<NginxServer> servers()
		{
			return servers;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder()
					.append("http {\n")
					.append(access_log)
					.append(root);
			servers.forEach(sb::append);
			sb.append("}\n");
			return sb.toString();
		}

		@Override
		public NginxServer addServer(final String host, final int port)
		{
			final NginxServer server = new NginxServerImpl();
			server.listen().giveValue(host + ":" + port);
			servers.add(server);
			return server;
		}

	}

	private static final class NginxServerImpl implements NginxServer
	{
		private final NginxVar listen;
		private final List<NginxLocation> locations;

		private NginxServerImpl()
		{
			listen = new NginxVarImpl("listen", 2);
			locations = new ArrayList<>();
		}

		@Override
		public NginxVar listen()
		{
			return listen;
		}

		@Override
		public List<NginxLocation> locations()
		{
			return locations;
		}

		@Override
		public NginxLocation addLocation(final String locPath)
		{
			final NginxLocation location = new NginxLocationImpl();
			location.location().giveValue(locPath);
			locations.add(location);
			return location;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder()
					.append(indent(1))
					.append("server {\n")
					.append(listen);
			locations.forEach(sb::append);
			sb.append(indent(1)).append("}\n");
			return sb.toString();
		}

	}

	private static final class NginxLocationImpl implements NginxLocation
	{
		private final NginxVar location;

		private NginxLocationImpl()
		{
			location = new NginxVarImpl("location", -1);
		}

		@Override
		public NginxVar location()
		{
			return location;
		}

		@Override
		public String toString()
		{
			return new StringBuilder()
					.append(indent(2))
					.append("location ")
					.append(location.value())
					.append(" {\n")
					.append(indent(2))
					.append("}\n")
					.toString();
		}

	}

	private static final class NginxEventsImpl implements NginxEvents
	{
		@Override
		public String toString()
		{
			return "events {\n}\n";
		}
	}

	private static final class NginxVarImpl implements NginxVar
	{
		private final String name;
		private final int indent;
		private final MutableOptional<String> value; // state

		private NginxVarImpl(final String name, final int indent)
		{
			this.name = OtherPreconditions.checkNotNullOrEmpty(name);
			this.indent = indent;
			this.value = MutableOptional.empty();
		}

		@Override
		public String value()
		{
			if (value.isPresent())
			{
				return value.get();
			}
			else
			{
				throw new IllegalStateException("Variable " + name + " not set");
			}
		}

		@Override
		public void giveValue(final String newValue)
		{
			value.add(newValue);
		}

		@Override
		public String toString()
		{
			return indent(indent) + name + " " + check(value()) + ";\n";
		}

		@Override
		public void giveFile(final File file)
		{
			giveValue(file.getAbsolutePath());
		}

	}

	private static final String check(final String value)
	{
		OtherPreconditions.checkNotNullOrEmpty(value);
		if (!VALUE_PATTERN.matcher(value).matches())
		{
			throw new IllegalArgumentException("Value might be invalid in nginx conf: " + value);
		}
		return value;
	}

	private static final String indent(final int n)
	{
		return Make.spaces(4 * n);
	}
}
