package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.pantheist.api.entity.model.ApiEntity;
import io.pantheist.api.entity.model.ListEntityItem;
import io.pantheist.api.java.model.ListJavaFileItem;
import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathJavaPackage;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ResponseType;

public class DiscoveryTest extends BaseTest
{
	private static final String JAVA_FILE = "java-file";
	private static final String TEXT_PLAIN = "text/plain";
	private static final String JAVA_PKG = "io.pantheist.examples";
	private static final String KIND_INTERFACE_RES = "/kind-schema/java-discoverable-interface";
	private static final String KIND_HIGH_PRECEDENCE_RES = "/kind-schema/java-discoverable-interface-higher-precedence";
	private static final String JAVA_INTLIST_NAME = "NonEmptyNonNegativeIntList";
	private static final String JAVA_INTLIST_RES = "/java-example/NonEmptyNonNegativeIntList";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";
	private static final String JAVA_BUTTER_NAME = "WithButterAnnotation";
	private static final String JAVA_BUTTER_RES = "/java-example/WithButterAnnotation";
	private static final String JAVA_CONSTRUCTOR_BUTTER_NAME = "ConstructedFromButter";
	private static final String JAVA_CONSTRUCTOR_BUTTER_NAME_RES = "/java-example/ConstructedFromButter";
	private static final String JAVA_CONSTRUCTOR_NON_BUTTER_NAME = "ConstructedFromRegular";
	private static final String JAVA_CONSTRUCTOR_NON_BUTTER_RES = "/java-example/ConstructedFromRegular";

	@Test
	public void javaEntity_isDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final ApiEntity entity = manage.entity(JAVA_INTLIST_NAME).getEntity();
		assertTrue("Entity should be marked as discovered", entity.discovered());
	}

	@Test
	public void javaEntity_wrongFileName_isNotDiscovered() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final ResponseType responseType1 = manage.entity(JAVA_INTLIST_NAME).getEntityResponseType();
		final ResponseType responseType2 = manage.entity("SomeOtherGarbage").getEntityResponseType();

		assertThat(responseType1, is(ResponseType.OK));
		assertThat(responseType2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaEntity_doesNotMatchKind_hasBaseKind() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		manage.kind("java-interface-file").putJsonResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_EMPTY_CLASS_NAME);
		java.data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final String kindUrl = manage.entity(JAVA_EMPTY_CLASS_NAME).getEntity().kindUrl();
		assertThat(kindUrl, is(baseKind.url()));
	}

	@Test
	public void discoveredJavaEntity_isListed() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_INTERFACE_RES);
		manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME).data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final List<ListEntityItem> result = manage.listEntities().childResources();

		assertThat(result.size(), is(1));
		assertThat(result.get(0).entityId(), is(JAVA_INTLIST_NAME));
		assertThat(result.get(0).discovered(), is(true));
	}

	@Test
	public void discoveredJavaEntity_isListed_underKind() throws Exception
	{
		manage.kind("java-interface-file").putJsonResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile jclass = manage.javaPackage(JAVA_PKG).file("EmptyClass");
		jclass.data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final ManagementPathJavaFile jinterface = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		jinterface.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final List<ListEntityItem> list = manage.kind("java-interface-file").listEntities().childResources();

		assertThat(list.size(), is(1));

		assertThat(list.get(0).entityId(), is(JAVA_INTLIST_NAME));
	}

	@Test
	public void javaFile_thatIsDiscovered_reportsKindUrl() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		final ManagementPathKind kind = manage.kind("java-interface-file");
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		assertThat(java.describeJavaFile().kindUrl(), is(baseKind.url()));

		kind.putJsonResource(KIND_INTERFACE_RES);

		assertThat(java.describeJavaFile().kindUrl(), is(kind.url()));
	}

	@Test
	public void javaFile_thatIsDiscovered_listedWithKind() throws Exception
	{
		final ManagementPathKind kind = manage.kind("java-interface-file");
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile java = pkg.file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);
		kind.putJsonResource(KIND_INTERFACE_RES);

		final List<ListJavaFileItem> list = pkg.listJavaFiles().childResources();
		assertThat(list.size(), is(1));
		assertThat(list.get(0).kindUrl(), is(kind.url()));
	}

	@Test
	public void javaFile_thatIsNotDiscovered_listedWithJavaFileKind() throws Exception
	{
		final ManagementPathKind javaFileKind = manage.kind(JAVA_FILE);
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile java = pkg.file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final List<ListJavaFileItem> list = pkg.listJavaFiles().childResources();
		assertThat(list.size(), is(1));
		assertThat(list.get(0).kindUrl(), is(javaFileKind.url()));
	}

	@Test
	public void java_discoverByAnnotation() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		final ManagementPathKind butteryKind = manage.kind("buttery");
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile butteryJava = pkg.file(JAVA_BUTTER_NAME);
		butteryJava.data().putResource(JAVA_BUTTER_RES, TEXT_PLAIN);
		final ManagementPathJavaFile otherJava = pkg.file(JAVA_INTLIST_NAME);
		otherJava.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);
		butteryKind.putJsonResource("/kind-schema/java-interface-with-butter-annotation");

		assertThat(butteryJava.describeJavaFile().kindUrl(), is(butteryKind.url()));
		assertThat(otherJava.describeJavaFile().kindUrl(), is(baseKind.url()));
	}

	@Test
	public void java_discoverByConstructorArg() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		final ManagementPathKind butteryKind = manage.kind("buttery");
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile butteryJava = pkg.file(JAVA_CONSTRUCTOR_BUTTER_NAME);
		butteryJava.data().putResource(JAVA_CONSTRUCTOR_BUTTER_NAME_RES, TEXT_PLAIN);
		final ManagementPathJavaFile otherJava = pkg.file(JAVA_CONSTRUCTOR_NON_BUTTER_NAME);
		otherJava.data().putResource(JAVA_CONSTRUCTOR_NON_BUTTER_RES, TEXT_PLAIN);
		butteryKind.putJsonResource("/kind-schema/java-constructor-arg-with-butter-annotation");

		assertThat(otherJava.describeJavaFile().kindUrl(), is(baseKind.url()));
		assertThat(butteryJava.describeJavaFile().kindUrl(), is(butteryKind.url()));
	}

	@Test
	public void precedence_higherNumberIsChosen() throws Exception
	{
		final ManagementPathKind lower = manage.kind("interface1");
		lower.putJsonResource(KIND_INTERFACE_RES);
		final ManagementPathKind higher = manage.kind("interface2");
		higher.putJsonResource(KIND_HIGH_PRECEDENCE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final ApiEntity entity = manage.entity(JAVA_INTLIST_NAME).getEntity();
		assertThat(entity.kindUrl(), is(higher.url()));
	}

	/**
	 * Just to check that the other test didn't pass by coincidence
	 */
	@Test
	public void precedence_higherNumberIsChosen_swapped() throws Exception
	{
		final ManagementPathKind higher = manage.kind("interface1");
		higher.putJsonResource(KIND_HIGH_PRECEDENCE_RES);
		final ManagementPathKind lower = manage.kind("interface2");
		lower.putJsonResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, TEXT_PLAIN);

		final ApiEntity entity = manage.entity(JAVA_INTLIST_NAME).getEntity();
		assertThat(entity.kindUrl(), is(higher.url()));
	}
}
