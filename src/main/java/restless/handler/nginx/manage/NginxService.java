package restless.handler.nginx.manage;

import restless.handler.nginx.model.NginxConfig;

public interface NginxService
{
	void configureAndStart(NginxConfig config);

	void stop();

	NginxConfig newConfig();
}
