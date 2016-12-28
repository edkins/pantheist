package restless.handler.nginx.manage;

import restless.common.util.ListView;
import restless.common.util.OptView;
import restless.common.util.Possible;

public interface NginxService
{
	void startOrRestart();

	void stop();

	boolean hasLocation(int port, String location);

	Possible<Void> deleteLocationAndRestart(int port, String location);

	Possible<ListView<String>> listLocations(int port);

	Possible<Void> putAndRestart(int port, String location, OptView<String> alias);
}
