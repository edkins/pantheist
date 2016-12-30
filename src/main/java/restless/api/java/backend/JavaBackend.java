package restless.api.java.backend;

import restless.api.java.model.ListJavaPkgResponse;
import restless.api.management.model.ListClassifierResponse;
import restless.common.util.Possible;

public interface JavaBackend
{
	Possible<Void> putJavaFile(String pkg, String file, String data);

	Possible<String> getJavaFile(String pkg, String file);

	ListJavaPkgResponse listJavaPackages();

	Possible<ListClassifierResponse> listJavaPackageClassifiers(String pkg);

}
