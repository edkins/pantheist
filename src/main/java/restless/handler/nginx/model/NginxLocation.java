package restless.handler.nginx.model;

public interface NginxLocation
{
	/**
	 * Represent this as a section in an nginx configuration file.
	 */
	@Override
	String toString();

	NginxVar location();

	NginxVar alias();
}
