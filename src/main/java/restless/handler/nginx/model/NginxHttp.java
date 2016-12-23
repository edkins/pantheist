package restless.handler.nginx.model;

import java.util.List;

public interface NginxHttp
{
	/**
	 * Represent this as a section in an nginx configuration file.
	 */
	@Override
	String toString();

	NginxVar access_log();

	NginxVar root();

	List<NginxServer> servers();

	NginxServer addServer(String host, int port);
}
