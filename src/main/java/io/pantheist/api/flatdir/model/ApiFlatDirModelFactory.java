package io.pantheist.api.flatdir.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface ApiFlatDirModelFactory
{
	ListFileItem listFileItem(@Assisted("url") String url, @Assisted("fileName") String fileName);

	ListFileResponse listFileResponse(List<ListFileItem> childResources);

	ListFlatDirItem listFlatDirItem(@Assisted("url") String url, @Assisted("relativePath") String relativePath);

	ListFlatDirResponse listFlatDirResponse(List<ListFlatDirItem> childResources);
}
