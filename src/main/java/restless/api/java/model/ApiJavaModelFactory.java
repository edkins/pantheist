package restless.api.java.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.AdditionalStructureItem;

public interface ApiJavaModelFactory
{
	ListJavaPkgItem listJavaPkgItem(@Assisted("url") String url);

	ListJavaPkgResponse listJavaPkgResponse(
			List<ListJavaPkgItem> childResources,
			List<AdditionalStructureItem> additionalStructure);

	ListFileItem listFileItem(@Assisted("url") String url);

	ListFileResponse listFileResponse(List<ListFileItem> childResources);
}
