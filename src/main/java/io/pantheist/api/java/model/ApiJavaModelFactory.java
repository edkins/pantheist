package io.pantheist.api.java.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;

public interface ApiJavaModelFactory
{
	ListJavaPkgItem listJavaPkgItem(@Assisted("url") String url);

	ListJavaPkgResponse listJavaPkgResponse(
			List<ListJavaPkgItem> childResources,
			CreateAction createAction,
			BindingAction bindingAction);

	ListJavaFileItem listFileItem(@Assisted("url") String url);

	ListJavaFileResponse listFileResponse(List<ListJavaFileItem> childResources);

	ApiJavaFile javaFile(
			DataAction dataAction,
			DeleteAction deleteAction,
			@Nullable @Assisted("kindUrl") String kindUrl);

	ApiJavaBinding javaBinding(@Assisted("location") String location);
}
