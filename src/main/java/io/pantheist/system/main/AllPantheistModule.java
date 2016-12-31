package io.pantheist.system.main;

import com.google.inject.AbstractModule;

import io.pantheist.api.entity.backend.ApiEntityBackendModule;
import io.pantheist.api.entity.model.ApiEntityModelModule;
import io.pantheist.api.entity.resource.ApiEntityResourceModule;
import io.pantheist.api.flatdir.backend.ApiFlatDirBackendModule;
import io.pantheist.api.flatdir.model.ApiFlatDirModelModule;
import io.pantheist.api.flatdir.resource.ApiFlatDirResourceModule;
import io.pantheist.api.java.backend.ApiJavaBackendModule;
import io.pantheist.api.java.model.ApiJavaModelModule;
import io.pantheist.api.java.resource.ApiJavaResourceModule;
import io.pantheist.api.kind.backend.ApiKindBackendModule;
import io.pantheist.api.kind.model.ApiKindModelModule;
import io.pantheist.api.kind.resource.ApiKindResourceModule;
import io.pantheist.api.management.backend.ApiManagementBackendModule;
import io.pantheist.api.management.model.ApiManagementModelModule;
import io.pantheist.api.management.resource.ApiManagementResourceModule;
import io.pantheist.api.schema.backend.ApiSchemaBackendModule;
import io.pantheist.api.schema.model.ApiSchemaModelModule;
import io.pantheist.api.schema.resource.ApiSchemaResourceModule;
import io.pantheist.common.api.model.CommonApiModelModule;
import io.pantheist.common.api.url.CommonApiUrlModule;
import io.pantheist.common.http.CommonHttpModule;
import io.pantheist.handler.entity.backend.HandlerEntityBackendModule;
import io.pantheist.handler.entity.model.HandlerEntityModuleModule;
import io.pantheist.handler.filesystem.backend.HandlerFilesystemBackendModule;
import io.pantheist.handler.java.backend.HandlerJavaBackendModule;
import io.pantheist.handler.java.model.HandlerJavaModelModule;
import io.pantheist.handler.kind.backend.HandlerKindBackendModule;
import io.pantheist.handler.kind.model.HandlerKindModelModule;
import io.pantheist.handler.nginx.manage.HandlerNginxManageModule;
import io.pantheist.handler.nginx.parser.HandlerNginxParserModule;
import io.pantheist.handler.schema.backend.HandlerSchemaBackendModule;
import io.pantheist.handler.schema.model.HandlerSchemaModelModule;
import io.pantheist.system.config.SystemConfigModule;
import io.pantheist.system.initializer.SystemInitializerModule;
import io.pantheist.system.inject.SystemInjectModule;
import io.pantheist.system.server.SystemServerModule;

public class AllPantheistModule extends AbstractModule
{
	private final String[] args;

	public AllPantheistModule(final String[] args)
	{
		this.args = args;
	}

	@Override
	protected void configure()
	{
		install(new ApiEntityBackendModule());
		install(new ApiEntityModelModule());
		install(new ApiEntityResourceModule());
		install(new ApiFlatDirBackendModule());
		install(new ApiFlatDirModelModule());
		install(new ApiFlatDirResourceModule());
		install(new ApiJavaBackendModule());
		install(new ApiJavaModelModule());
		install(new ApiJavaResourceModule());
		install(new ApiKindBackendModule());
		install(new ApiKindModelModule());
		install(new ApiKindResourceModule());
		install(new ApiManagementBackendModule());
		install(new ApiManagementModelModule());
		install(new ApiManagementResourceModule());
		install(new ApiSchemaBackendModule());
		install(new ApiSchemaModelModule());
		install(new ApiSchemaResourceModule());
		install(new CommonApiModelModule());
		install(new CommonApiUrlModule());
		install(new CommonHttpModule());
		install(new HandlerEntityBackendModule());
		install(new HandlerEntityModuleModule());
		install(new HandlerFilesystemBackendModule());
		install(new HandlerJavaBackendModule());
		install(new HandlerJavaModelModule());
		install(new HandlerKindBackendModule());
		install(new HandlerKindModelModule());
		install(new HandlerNginxManageModule());
		install(new HandlerNginxParserModule());
		install(new HandlerSchemaBackendModule());
		install(new HandlerSchemaModelModule());
		install(new SystemConfigModule(args));
		install(new SystemInitializerModule());
		install(new SystemInjectModule());
		install(new SystemServerModule());
	}
}
