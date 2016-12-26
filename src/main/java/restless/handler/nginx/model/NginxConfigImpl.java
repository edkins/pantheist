package restless.handler.nginx.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.collect.Lists;

import restless.common.util.Make;
import restless.common.util.MutableOptional;
import restless.common.util.OtherPreconditions;
import restless.handler.filesystem.backend.FsPath;
import restless.system.config.RestlessConfig;

final class NginxConfigImpl implements NginxConfig
{
	private static final Pattern VALUE_PATTERN = Pattern.compile("[a-zA-Z0-9-_/.:]+");
	private final NginxVar pid;
	private final NginxVar error_log;
	private final NginxEvents events;
	private final NginxHttp http;
	private final RestlessConfig restlessConfig;

	@Inject
	NginxConfigImpl(final RestlessConfig restlessConfig)
	{
		this.restlessConfig = checkNotNull(restlessConfig);
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

	private final class NginxHttpImpl implements NginxHttp
	{
		private final NginxVar access_log;
		private final NginxVar root;
		private final NginxVarImpl charset;
		private final List<NginxServer> servers;

		private NginxHttpImpl()
		{
			access_log = new NginxVarImpl("access_log", 1);
			root = new NginxVarImpl("root", 1);
			charset = new NginxVarImpl("charset", 1);
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
					.append(root)
					.append(charset);
			servers.forEach(sb::append);
			sb.append("}\n");
			return sb.toString();
		}

		@Override
		public NginxServer addServer(final int port)
		{
			final NginxServer server = new NginxServerImpl(port);
			servers.add(server);
			return server;
		}

		@Override
		public NginxVar charset()
		{
			return charset;
		}

	}

	private final class NginxServerImpl implements NginxServer
	{
		private final NginxVar listen;
		private final List<NginxLocation> locations;
		private final int port;

		private NginxServerImpl(final int port)
		{
			listen = new NginxVarImpl("listen", 2);
			listen.giveValue("127.0.0.1:" + port);
			locations = new ArrayList<>();
			this.port = port;
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
			final NginxLocation location = new NginxLocationImpl("");
			location.location().giveValue(locPath);
			locations.add(location);
			return location;
		}

		@Override
		public NginxLocation addLocationEquals(final String locPath)
		{
			final NginxLocation location = new NginxLocationImpl("=");
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

		@Override
		public boolean isEmpty()
		{
			return locations.isEmpty();
		}

		@Override
		public int port()
		{
			return port;
		}

	}

	private final class NginxLocationImpl implements NginxLocation
	{
		private final NginxVar location;
		private final NginxVar alias;
		private final String oper;

		private NginxLocationImpl(final String oper)
		{
			location = new NginxVarImpl("location", -1);
			alias = new NginxVarImpl("alias", 3);
			this.oper = oper;
		}

		@Override
		public NginxVar location()
		{
			return location;
		}

		@Override
		public NginxVar alias()
		{
			return alias;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder()
					.append(indent(2))
					.append("location ");
			if (!oper.isEmpty())
			{
				sb.append(oper).append(" ");
			}
			return sb.append(location.value())
					.append(" {\n")
					.append(alias)
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

	private final class NginxVarImpl implements NginxVar
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
			if (value.isPresent())
			{
				return indent(indent) + name + " " + check(value.get()) + ";\n";
			}
			else
			{
				return "";
			}
		}

		@Override
		public void giveDirPath(final FsPath path)
		{
			giveValue(path.in(restlessConfig.dataDir()).getAbsolutePath() + "/");
		}

		@Override
		public void giveFilePath(final FsPath path)
		{
			giveValue(path.in(restlessConfig.dataDir()).getAbsolutePath());
		}

		@Override
		public void giveAbsoluteDirPath(final String handlerPath)
		{
			OtherPreconditions.checkNotNullOrEmpty(handlerPath);
			if (!handlerPath.startsWith("/"))
			{
				throw new IllegalArgumentException("Filesystem path must start with slash");
			}
			else if (handlerPath.endsWith("/"))
			{
				giveValue(handlerPath);
			}
			else
			{
				giveValue(handlerPath + "/");
			}
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

	@Override
	public NginxServer httpServer()
	{
		if (http.servers().size() != 1)
		{
			throw new IllegalStateException("Must be exactly one server");
		}
		return http.servers().get(0);
	}

	@Override
	public boolean isEmpty()
	{
		return http.servers().stream().allMatch(NginxServer::isEmpty);
	}

	@Override
	public List<Integer> ports()
	{
		return Lists.transform(http.servers(), NginxServer::port);
	}
}
