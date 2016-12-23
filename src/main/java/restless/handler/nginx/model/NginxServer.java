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
}
