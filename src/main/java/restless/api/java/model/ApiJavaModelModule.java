package restless.api.java.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiJavaModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiJavaModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListJavaPkgItem.class, ListJavaPkgItemImpl.class)
				.implement(ListJavaPkgResponse.class, ListJavaPkgResponseImpl.class)
				.implement(ListFileItem.class, ListFileItemImpl.class)
				.implement(ListFileResponse.class, ListFileResponseImpl.class)
				.implement(ApiJavaFile.class, ApiJavaFileImpl.class)
				.implement(ApiJavaBinding.class, ApiJavaBindingImpl.class)
				.build(ApiJavaModelFactory.class));
	}

}
