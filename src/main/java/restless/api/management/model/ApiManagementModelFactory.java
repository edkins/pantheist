package restless.api.management.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import restless.handler.uri.ListClassifierItem;

public interface ApiManagementModelFactory
{
	ListConfigItem listConfigItem(@Assisted("url") String url);

	ListConfigResponse listConfigResponse(List<ListConfigItem> childResources);

	ListClassifierResponse listClassifierResponse(List<ListClassifierItem> childResources);

	ListJavaPkgItem listJavaPkgItem(@Assisted("url") String url);

	ListJavaPkgResponse listJavaPkgResponse(List<ListJavaPkgItem> childResources);
}
