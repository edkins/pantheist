package io.pantheist.api.flatdir.backend;

import io.pantheist.api.flatdir.model.ApiFlatDirFile;
import io.pantheist.api.flatdir.model.ListFileResponse;
import io.pantheist.api.flatdir.model.ListFlatDirResponse;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.util.Possible;

public interface FlatDirBackend
{
	Possible<ListFileResponse> listFiles(String dir);

	Possible<ListClassifierResponse> listFlatDirClassifiers(String dir);

	ListFlatDirResponse listFlatDirs();

	Possible<ApiFlatDirFile> getFileInfo(String dir, String file);

	Possible<String> getFileData(String dir, String file);

	Possible<Void> putFileDataString(String dir, String file, String data, boolean failIfExists);
}
