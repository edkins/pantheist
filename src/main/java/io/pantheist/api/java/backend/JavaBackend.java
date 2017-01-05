package io.pantheist.api.java.backend;

import io.pantheist.api.java.model.ApiJavaBinding;
import io.pantheist.api.java.model.ApiJavaFile;
import io.pantheist.api.java.model.ListJavaFileResponse;
import io.pantheist.api.java.model.ListJavaPkgResponse;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.util.Possible;

public interface JavaBackend
{
	Possible<Void> putJavaFile(String pkg, String file, String code);

	Possible<String> getJavaFile(String pkg, String file);

	ListJavaPkgResponse listJavaPackages();

	Possible<ListClassifierResponse> listJavaPackageClassifiers(String pkg);

	Possible<ListJavaFileResponse> listFilesInPackage(String pkg);

	Possible<ApiJavaFile> describeJavaFile(String pkg, String file);

	Possible<Void> deleteJavaFile(String pkg, String file);

	Possible<Void> putJavaBinding(ApiJavaBinding request);

	ApiJavaBinding getJavaBinding();

	Possible<String> postJava(String code);
}
