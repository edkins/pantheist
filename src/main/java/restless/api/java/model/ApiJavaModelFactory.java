package restless.api.java.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.CreateAction;
import restless.common.api.model.DataAction;
import restless.common.api.model.DeleteAction;

public interface ApiJavaModelFactory
{
	ListJavaPkgItem listJavaPkgItem(@Assisted("url") String url);

	ListJavaPkgResponse listJavaPkgResponse(
			List<ListJavaPkgItem> childResources,
			CreateAction createAction);

	ListFileItem listFileItem(@Assisted("url") String url);

	ListFileResponse listFileResponse(List<ListFileItem> childResources);

	ApiJavaFile javaFile(DataAction dataAction, DeleteAction deleteAction);
}
