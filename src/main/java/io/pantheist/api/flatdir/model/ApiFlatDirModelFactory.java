package io.pantheist.api.flatdir.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.DataAction;

public interface ApiFlatDirModelFactory
{
	ListFileItem listFileItem(
			@Assisted("url") String url,
			@Assisted("fileName") String fileName,
			@Assisted("kindUrl") String kindUrl);

	ListFileResponse listFileResponse(List<ListFileItem> childResources);

	ListFlatDirItem listFlatDirItem(
			@Assisted("url") String url,
			@Assisted("relativePath") String relativePath,
			@Assisted("kindUrl") String kindUrl);

	ListFlatDirResponse listFlatDirResponse(List<ListFlatDirItem> childResources);

	ApiFlatDirFile file(
			@Assisted DataAction dataAction,
			@Assisted("kindUrl") String kindUrl);
}
