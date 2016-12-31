package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ListEntityItem;
import restless.client.api.ManagementPathJavaFile;
import restless.client.api.ManagementPathKind;
import restless.client.api.ResponseType;

public class DiscoveryTest extends BaseTest
{
	private static final String JAVA_PKG = "restless.examples";
	private static final String KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES = "/kind-schema/java-discoverable-interface";
	private static final String JAVA_INTLIST_NAME = "NonEmptyNonNegativeIntList";
	private static final String JAVA_INTLIST_RES = "/java-example/NonEmptyNonNegativeIntList";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";

	@Test
	public void javaEntity_isDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final ApiEntity entity = manage.entity(JAVA_INTLIST_NAME).getEntity();
		assertTrue("Entity should be marked as discovered", entity.discovered());
	}

	@Test
	public void javaEntity_wrongFileName_isNotDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
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

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_EMPTY_CLASS_NAME);
		java.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final ResponseType responseType = manage.entity(JAVA_EMPTY_CLASS_NAME).getEntityResponseType();

		assertThat(responseType, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void discoveredJavaEntity_isListed() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);
		manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME).data().putResource(JAVA_INTLIST_RES, "text/plain");

		final List<ListEntityItem> result = manage.listEntities().childResources();

		assertThat(result.size(), is(1));
		assertThat(result.get(0).entityId(), is(JAVA_INTLIST_NAME));
		assertThat(result.get(0).discovered(), is(true));
	}

	@Test
	public void discoveredJavaEntity_isListed_underKind() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		final ManagementPathJavaFile jclass = manage.javaPackage(JAVA_PKG).file("EmptyClass");
		jclass.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final ManagementPathJavaFile jinterface = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		jinterface.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final List<ListEntityItem> list = manage.kind("java-interface-file").listEntities().childResources();

		assertThat(list.size(), is(1));

		assertThat(list.get(0).entityId(), is(JAVA_INTLIST_NAME));
	}

	@Test
	public void javaFile_thatIsDiscovered_reportsKindUrl() throws Exception
	{
		final ManagementPathKind kind = manage.kind("java-interface-file");
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		assertThat(java.describeJavaFile().kindUrl(), nullValue());

		kind.putJsonResource(KIND_SCHEMA_JAVA_DISCOVERABLE_INTERFACE_RES);

		assertThat(java.describeJavaFile().kindUrl(), is(kind.url()));
	}
}
