package restless.handler.nginx.model;

import java.util.List;

public interface NginxServer
{
	/**
	 * Represent this as a section in an nginx configuration file.
	 */
	@Override
	String toString();

	NginxVar listen();

	List<NginxLocation> locations();

	NginxLocation addLocationEquals(String location);

	NginxLocation addLocation(String location);

	/**
	 * Returns true if this server is not serving any locations.
	 */
	boolean isEmpty();

	int port();
}
