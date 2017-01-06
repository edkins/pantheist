package io.pantheist.common.api.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CommonApiModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(CommonApiModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListClassifierItem.class, ListClassifierItemImpl.class)
				.implement(ListClassifierResponse.class, ListClassifierResponseImpl.class)
				.implement(CreateAction.class, CreateActionImpl.class)
				.implement(DataAction.class, DataActionImpl.class)
				.implement(DeleteAction.class, DeleteActionImpl.class)
				.implement(BindingAction.class, BindingActionImpl.class)
				.implement(KindedMime.class, KindedMimeImpl.class)
				.build(CommonApiModelFactory.class));
	}

}
