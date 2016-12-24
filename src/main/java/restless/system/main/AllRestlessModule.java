package restless.system.main;

import com.google.inject.PrivateModule;

import restless.api.management.backend.ApiManagementBackendModule;
import restless.api.management.resource.ApiManagementResourceModule;
import restless.glue.initializer.GlueInitializerModule;
import restless.glue.initializer.Initializer;
import restless.glue.nginx.filesystem.GlueNginxFilesystemModule;
import restless.handler.binding.backend.HandlerBindingBackendModule;
import restless.handler.binding.model.HandlerBindingModelModule;
import restless.handler.filesystem.backend.HandlerFilesystemBackendModule;
import restless.handler.nginx.manage.HandlerNginxManageModule;
import restless.handler.nginx.model.HandlerNginxModelModule;
import restless.system.config.SystemConfigModule;
import restless.system.inject.SystemInjectModule;
import restless.system.server.SystemServerModule;

public class AllRestlessModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(Initializer.class);
		install(new ApiManagementBackendModule());
		install(new ApiManagementResourceModule());
		install(new GlueInitializerModule());
		install(new GlueNginxFilesystemModule());
		install(new HandlerBindingBackendModule());
		install(new HandlerBindingModelModule());
		install(new HandlerFilesystemBackendModule());
		install(new HandlerNginxManageModule());
		install(new HandlerNginxModelModule());
		install(new SystemConfigModule());
		install(new SystemInjectModule());
		install(new SystemServerModule());
	}

}
