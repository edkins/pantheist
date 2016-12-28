package restless.system.main;

import com.google.inject.PrivateModule;

import restless.api.management.backend.ApiManagementBackendModule;
import restless.api.management.model.ApiManagementModelModule;
import restless.api.management.resource.ApiManagementResourceModule;
import restless.glue.initializer.GlueInitializerModule;
import restless.glue.initializer.Initializer;
import restless.handler.binding.model.HandlerBindingModelModule;
import restless.handler.filesystem.backend.HandlerFilesystemBackendModule;
import restless.handler.java.backend.HandlerJavaBackendModule;
import restless.handler.nginx.manage.HandlerNginxManageModule;
import restless.handler.nginx.parser.HandlerNginxParserModule;
import restless.handler.schema.backend.HandlerSchemaBackendModule;
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
		install(new ApiManagementModelModule());
		install(new ApiManagementResourceModule());
		install(new GlueInitializerModule());
		install(new HandlerBindingModelModule());
		install(new HandlerFilesystemBackendModule());
		install(new HandlerJavaBackendModule());
		install(new HandlerNginxManageModule());
		install(new HandlerNginxParserModule());
		install(new HandlerSchemaBackendModule());
		install(new SystemConfigModule());
		install(new SystemInjectModule());
		install(new SystemServerModule());
	}

}
