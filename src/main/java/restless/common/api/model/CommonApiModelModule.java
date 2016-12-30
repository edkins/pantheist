package restless.common.api.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CommonApiModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(CommonApiModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(AdditionalStructureItem.class, AdditionalStructureItemImpl.class)
				.implement(ListClassifierItem.class, ListClassifierItemImpl.class)
				.build(CommonApiModelFactory.class));
	}

}
