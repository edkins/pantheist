package restless.client.api;

import restless.api.java.model.ApiJavaBinding;

public interface ManagementPathJavaBinding
{
	ApiJavaBinding getJavaBinding();

	void setJavaBinding(String location);
}
