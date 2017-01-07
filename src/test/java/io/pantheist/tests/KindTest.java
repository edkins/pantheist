package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import io.pantheist.api.entity.model.ListEntityItem;
import io.pantheist.api.java.model.ListJavaFileItem;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathJavaPackage;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ResponseType;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class KindTest
{
	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;

	private static final String JAVA_FILE = "java-file";
	private static final String APPLICATION_JSON = "application/json";
	private static final String JAVA_PKG = "io.pantheist.examples";
	private static final String KIND_INTERFACE_RES = "/kind-schema/java-discoverable-interface";
	private static final String JAVA_INTLIST_NAME = "NonEmptyNonNegativeIntList";
	private static final String JAVA_INTLIST_RES = "/java-example/NonEmptyNonNegativeIntList";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_BUTTER_NAME = "WithButterAnnotation";
	private static final String JAVA_BUTTER_RES = "/java-example/WithButterAnnotation";
	private static final String JAVA_BUTTER_SUGAR_NAME = "WithButterSugar";
	private static final String JAVA_CONSTRUCTOR_BUTTER_NAME = "ConstructedFromButter";
	private static final String JAVA_CONSTRUCTOR_BUTTER_NAME_RES = "/java-example/ConstructedFromButter";
	private static final String JAVA_CONSTRUCTOR_NON_BUTTER_NAME = "ConstructedFromRegular";
	private static final String JAVA_CONSTRUCTOR_NON_BUTTER_RES = "/java-example/ConstructedFromRegular";
	private static final String JAVA_SUGAR_NAME = "WithSugar";
	private static final String KIND_SCHEMA_RES = "/kind-schema/kind-test-example";
	private static final String KIND_SCHEMA_SYSTEM_RES = "/kind-schema/kind-test-example-system";
	private static final String KIND_EXAMPLE_CORRECT_ID_RES = "/kind-schema/kind-test-example-correct-id";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
	}

	@Test
	public void kind_canReadBack() throws Exception
	{
		manage.kind("my-kind").putKindResource(KIND_SCHEMA_RES);

		final Kind kind = manage.kind("my-kind").getKind();

		assertThat(kind.kindId(), is("my-kind"));
		assertThat(kind.presentation().iconUrl(), is("http://example.com/icon.png"));
		assertThat(kind.presentation().openIconUrl(), is("http://example.com/icon2.png"));
		assertFalse("This kind not part of system", kind.partOfSystem());
	}

	@Test
	public void kind_getMetaKind() throws Exception
	{
		manage.kind("my-kind").putKindResource(KIND_SCHEMA_RES);

		assertThat(manage.kind("my-kind").headKindUrl(), is(manage.kind("kind").url()));
	}

	@Test
	public void kind_canBeTaggedAsSystem() throws Exception
	{
		manage.kind("my-kind").putKindResource(KIND_SCHEMA_SYSTEM_RES);

		final Kind kind = manage.kind("my-kind").getKind();

		assertTrue("This kind should be part of system", kind.partOfSystem());
	}

	@Test
	public void kind_isListed() throws Exception
	{
		final ManagementPathKind kind = manage.kind("my-kind");
		kind.putKindResource(KIND_SCHEMA_RES);

		final List<ListEntityItem> list = manage.entitiesWithKind("kind").listEntities().childResources();

		final ListEntityItem item = list.stream().filter(k -> k.url().equals(kind.url())).findFirst().get();
		assertThat(item.url(), is(kind.url()));
	}

	@Test
	public void discoveredJavaEntity_isListed_underKind() throws Exception
	{
		final ManagementPathKind kind = manage.kind("java-interface-file");
		kind.putKindResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile jclass = manage.javaPackage(JAVA_PKG).file("EmptyClass");
		jclass.putJavaResource(JAVA_EMPTY_CLASS_RES);

		final ManagementPathJavaFile jinterface = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		jinterface.putJavaResource(JAVA_INTLIST_RES);

		final List<ListEntityItem> list = manage.entitiesWithKind("java-interface-file")
				.listEntities()
				.childResources();

		assertThat(list.size(), is(1));

		assertThat(list.get(0).entityId(), is("io.pantheist.examples." + JAVA_INTLIST_NAME));
		assertThat(list.get(0).kindUrl(), is(kind.url()));
	}

	@Test
	public void javaFile_thatIsDiscovered_reportsKindUrl() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		final ManagementPathKind kind = manage.kind("java-interface-file");
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.putJavaResource(JAVA_INTLIST_RES);

		assertThat(java.headKindUrl(), is(baseKind.url()));

		kind.putKindResource(KIND_INTERFACE_RES);

		assertThat(java.headKindUrl(), is(kind.url()));
	}

	@Test
	public void javaFile_thatIsDiscovered_listedWithKind() throws Exception
	{
		final ManagementPathKind kind = manage.kind("java-interface-file");
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile java = pkg.file(JAVA_INTLIST_NAME);
		java.putJavaResource(JAVA_INTLIST_RES);
		kind.putKindResource(KIND_INTERFACE_RES);

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
		java.putJavaResource(JAVA_INTLIST_RES);

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
		butteryJava.putJavaResource(JAVA_BUTTER_RES);
		final ManagementPathJavaFile otherJava = pkg.file(JAVA_INTLIST_NAME);
		otherJava.putJavaResource(JAVA_INTLIST_RES);
		butteryKind.putKindResource("/kind-schema/java-interface-with-butter-annotation");

		assertThat(butteryJava.headKindUrl(), is(butteryKind.url()));
		assertThat(otherJava.headKindUrl(), is(baseKind.url()));
	}

	@Test
	public void java_discoverByConstructorArg() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		final ManagementPathKind butteryKind = manage.kind("buttery");
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile butteryJava = pkg.file(JAVA_CONSTRUCTOR_BUTTER_NAME);
		butteryJava.putJavaResource(JAVA_CONSTRUCTOR_BUTTER_NAME_RES);
		final ManagementPathJavaFile otherJava = pkg.file(JAVA_CONSTRUCTOR_NON_BUTTER_NAME);
		otherJava.putJavaResource(JAVA_CONSTRUCTOR_NON_BUTTER_RES);
		butteryKind.putKindResource("/kind-schema/java-constructor-arg-with-butter-annotation");

		assertThat(otherJava.headKindUrl(), is(baseKind.url()));
		assertThat(butteryJava.headKindUrl(), is(butteryKind.url()));
	}

	@Test
	public void conflictingKinds_oneIsChosen() throws Exception
	{
		final ManagementPathKind kind1 = manage.kind("interface1");
		kind1.putKindResource(KIND_INTERFACE_RES);
		final ManagementPathKind kind2 = manage.kind("interface2");
		kind2.putKindResource(KIND_INTERFACE_RES);

		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.putJavaResource(JAVA_INTLIST_RES);

		assertThat(java.headKindUrl(), isOneOf(kind1.url(), kind2.url()));
	}

	@Test
	public void subkind_needsToMatchParent_andSelf() throws Exception
	{
		final ManagementPathKind baseKind = manage.kind(JAVA_FILE);
		final ManagementPathKind butterKind = mainRule.putKindResource("java-interface-with-butter-annotation");
		final ManagementPathKind butterSugarKind = mainRule.putKindResource("java-interface-butter-sugar");
		final ManagementPathJavaFile javaButter = mainRule.putJavaResource(JAVA_BUTTER_NAME);
		final ManagementPathJavaFile javaSugar = mainRule.putJavaResource(JAVA_SUGAR_NAME);
		final ManagementPathJavaFile javaButterSugar = mainRule.putJavaResource(JAVA_BUTTER_SUGAR_NAME);

		assertThat(javaButter.headKindUrl(), is(butterKind.url()));
		assertThat(javaSugar.headKindUrl(), is(baseKind.url()));
		assertThat(javaButterSugar.headKindUrl(), is(butterSugarKind.url()));
	}

	@Test
	public void kind_canPost_andReadBack() throws Exception
	{
		manage.kind("kind").postCreate(mainRule.resource(KIND_EXAMPLE_CORRECT_ID_RES), APPLICATION_JSON);

		final Kind kind = manage.kind("my-kind").getKind();

		assertThat(kind.kindId(), is("my-kind"));
		assertThat(kind.schema().identification().get("parentKind").textValue(), is("java-file"));
	}

	@Test
	public void kind_postTwice_sameId_secondOneFails() throws Exception
	{
		final ResponseType response1 = manage.kind("kind")
				.postCreateResponseType(mainRule.resource(KIND_EXAMPLE_CORRECT_ID_RES), APPLICATION_JSON);
		final ResponseType response2 = manage.kind("kind")
				.postCreateResponseType(mainRule.resource(KIND_EXAMPLE_CORRECT_ID_RES), APPLICATION_JSON);

		assertThat(response1, is(ResponseType.CREATED));
		assertThat(response2, is(ResponseType.CONFLICT));
	}
}
