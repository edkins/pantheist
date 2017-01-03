package io.pantheist.testclient.api;

import io.pantheist.api.flatdir.model.ApiFlatDirFile;

public interface ManagementFlatDirFilePath
{
	ApiFlatDirFile describeFlatDirFile();

	ResponseType getFlatDirFileResponseType();

	ManagementData data();

}
