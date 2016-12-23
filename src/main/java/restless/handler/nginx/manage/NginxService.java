package restless.handler.nginx.manage;

public interface NginxService
{
	void configureAndStart();

	void stop();
}
