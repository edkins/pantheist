package io.pantheist.api.java.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CreateAction;

public interface ApiJavaModelFactory
{
	ListJavaPkgItem listJavaPkgItem(
			@Assisted("url") String url,
			@Assisted("name") String name,
			@Assisted("kindUrl") String kindUrl);

	ListJavaPkgResponse listJavaPkgResponse(
			List<ListJavaPkgItem> childResources,
			CreateAction createAction,
			BindingAction bindingAction);

	ListJavaFileItem listFileItem(
			@Assisted("url") String url,
			@Assisted("name") String name,
			@Assisted("kindUrl") String kindUrl);

	ListJavaFileResponse listFileResponse(List<ListJavaFileItem> childResources);

	ApiJavaBinding javaBinding(@Assisted("location") String location);
}
