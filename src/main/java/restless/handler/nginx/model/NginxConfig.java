package restless.handler.nginx.model;

public interface NginxConfig
{
	/**
	 * Represent this as an nginx configuration file.
	 */
	@Override
	String toString();

	NginxVar pid();

	NginxVar error_log();

	NginxEvents events();

	NginxHttp http();

	/**
	 * Convenience method for accessing the single http server.
	 *
	 * @throws IllegalStateException
	 *             if there isn't exactly one server.
	 */
	NginxServer httpServer();
}
