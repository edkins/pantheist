package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ListEntityItem;
import restless.api.kind.model.ApiKind;
import restless.api.kind.model.ListKindItem;
import restless.client.api.ManagementPathJavaFile;
import restless.client.api.ManagementPathKind;
import restless.client.api.ResponseType;
import restless.handler.kind.model.JavaKind;
import restless.handler.kind.model.KindLevel;

public class KindTest extends BaseTest
{
	private static final String JAVA_PKG = "restless.examples";
	private static final String JAVA_INTLIST_NAME = "NonEmptyNonNegativeIntList";
	private static final String JAVA_INTLIST_RES = "/java-example/NonEmptyNonNegativeIntList";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String KIND_SCHEMA_RES = "/kind-schema/kind-test-example";
	private static final String KIND_EXAMPLE_CORRECT_ID_RES = "/kind-schema/kind-test-example-correct-id";
	private static final String KIND_EXAMPLE_GARBAGE_ID_RES = "/kind-schema/kind-test-example-garbage-id";

	@Test
	public void kind_canReadBack() throws Exception
	{
		manage.kind("my-kind").putJsonResource(KIND_SCHEMA_RES);

		final ApiKind kind = manage.kind("my-kind").getKind();

		assertThat(kind.level(), is(KindLevel.entity));
		assertThat(kind.java().required(), is(true));
		assertThat(kind.java().javaKind(), is(JavaKind.INTERFACE));
		assertThat(kind.kindId(), is("my-kind"));
	}

	@Test
	public void kind_isListed() throws Exception
	{
		final ManagementPathKind kind = manage.kind("my-kind");
		kind.putJsonResource(KIND_SCHEMA_RES);

		final List<ListKindItem> list = manage.listKinds().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(kind.url()));
	}

	@Test
	public void kind_withWrongId_rejected() throws Exception
	{
		final ResponseType response1 = manage.kind("my-kind").putJsonResourceResponseType(KIND_EXAMPLE_GARBAGE_ID_RES);
		final ResponseType response2 = manage.kind("my-kind").putJsonResourceResponseType(KIND_EXAMPLE_CORRECT_ID_RES);

		assertThat(response1, is(ResponseType.BAD_REQUEST));
		assertThat(response2, is(ResponseType.NO_CONTENT));
	}

	@Test
	public void entity_withKind_isValid() throws Exception
	{
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final ManagementPathKind kindPath = manage.kind("my-kind");
		kindPath.putJsonResource(KIND_SCHEMA_RES);

		manage.entity("my-entity").putEntity(kindPath.url(), null, java.url());

		final ApiEntity result = manage.entity("my-entity").getEntity();
		assertThat(result.kindUrl(), is(kindPath.url()));
		assertTrue("Entity should be valid", result.valid());
	}

	@Test
	public void entity_withKind_isListed() throws Exception
	{
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final ManagementPathKind kindPath = manage.kind("my-kind");
		kindPath.putJsonResource(KIND_SCHEMA_RES);

		manage.entity("my-entity").putEntity(kindPath.url(), null, java.url());

		final List<ListEntityItem> list = kindPath.listEntities().childResources();
		assertThat(list.size(), is(1));
		assertThat(list.get(0).entityId(), is("my-entity"));
	}

	@Test
	public void entity_withDifferentKind_isNotListed() throws Exception
	{
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file(JAVA_INTLIST_NAME);
		java.data().putResource(JAVA_INTLIST_RES, "text/plain");

		final ManagementPathKind kindPath = manage.kind("my-kind");
		kindPath.putJsonResource(KIND_SCHEMA_RES);

		final ManagementPathKind kindPath2 = manage.kind("my-kind2");
		kindPath2.putJsonResource(KIND_SCHEMA_RES);

		manage.entity("my-entity").putEntity(kindPath.url(), null, java.url());

		final List<ListEntityItem> list = kindPath2.listEntities().childResources();
		assertThat(list.size(), is(0));
	}

	@Test
	public void entity_kindSaysJava_noJava_invalid() throws Exception
	{
		final ManagementPathKind kindPath = manage.kind("my-kind");
		kindPath.putJsonResource(KIND_SCHEMA_RES);

		manage.entity("my-entity").putEntity(kindPath.url(), null, null);

		final ApiEntity result = manage.entity("my-entity").getEntity();
		assertFalse("Entity should be invalid", result.valid());
	}

	@Test
	public void entity_kindSaysInterface_actuallyClass_invalid() throws Exception
	{
		final ManagementPathJavaFile java = manage.javaPackage(JAVA_PKG).file("EmptyClass");
		java.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final ManagementPathKind kindPath = manage.kind("my-kind");
		kindPath.putJsonResource(KIND_SCHEMA_RES);

		manage.entity("my-entity").putEntity(kindPath.url(), null, java.url());

		final ApiEntity result = manage.entity("my-entity").getEntity();
		assertFalse("Entity should be invalid", result.valid());
	}
}
