package restless.api.flatdir.backend;

import restless.api.flatdir.model.ListFileResponse;
import restless.api.flatdir.model.ListFlatDirResponse;
import restless.common.api.model.ListClassifierResponse;
import restless.common.util.Possible;

public interface FlatDirBackend
{
	Possible<ListFileResponse> listFiles(String dir);

	Possible<ListClassifierResponse> listFlatDirClassifiers(String dir);

	ListFlatDirResponse listFlatDirs();
}
