package io.pantheist.testclient.api;

import io.pantheist.api.java.model.ApiJavaBinding;

public interface ManagementPathJavaBinding
{
	ApiJavaBinding getJavaBinding();

	void setJavaBinding(String location);
}
