package restless.api.kind.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiKindModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiKindModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ApiEntity.class, ApiEntityImpl.class)
				.implement(ApiComponent.class, ApiComponentImpl.class)
				.implement(ApiKind.class, ApiKindImpl.class)
				.implement(ListComponentItem.class, ListComponentItemImpl.class)
				.implement(ListComponentResponse.class, ListComponentResponseImpl.class)
				.implement(ListEntityItem.class, ListEntityItemImpl.class)
				.implement(ListEntityResponse.class, ListEntityResponseImpl.class)
				.build(ApiKindModelFactory.class));
	}

}
