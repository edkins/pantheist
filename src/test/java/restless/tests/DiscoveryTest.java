package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import restless.api.kind.model.ApiEntity;
import restless.client.api.ManagementPathJavaFile;
import restless.client.api.ResponseType;

public class DiscoveryTest extends BaseTest
{
	private static final String KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES = "/kind-schema/java-discoverable-interface";
	private static final String JAVA_INTLIST_NAME = "NonEmptyNonNegativeIntList";
	private static final String JAVA_INTLIST_RES = "/java-example/NonEmptyNonNegativeIntList";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";

	@Test
	public void javaEntity_isDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage("restless.examples").file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final ApiEntity entity = manage.entity(JAVA_INTLIST_NAME).getEntity();
		assertTrue("Entity should be marked as discovered", entity.discovered());
	}

	@Test
	public void javaEntity_wrongFileName_isNotDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage("restless.examples").file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final ResponseType responseType1 = manage.entity(JAVA_INTLIST_NAME).getEntityResponseType();
		final ResponseType responseType2 = manage.entity("SomeOtherGarbage").getEntityResponseType();

		assertThat(responseType1, is(ResponseType.OK));
		assertThat(responseType2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaEntity_doesNotMatchKind_isNotDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage("restless.examples").file(JAVA_EMPTY_CLASS_NAME);
		java.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final ResponseType responseType = manage.entity(JAVA_EMPTY_CLASS_NAME).getEntityResponseType();

		assertThat(responseType, is(ResponseType.NOT_FOUND));
	}
}
