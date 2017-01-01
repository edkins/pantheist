package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ListKindItem;
import io.pantheist.handler.kind.model.JavaKind;
import io.pantheist.handler.kind.model.KindLevel;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ResponseType;

public class KindTest extends BaseTest
{
	private static final String KIND_SCHEMA_RES = "/kind-schema/kind-test-example";
	private static final String KIND_SCHEMA_SYSTEM_RES = "/kind-schema/kind-test-example-system";
	private static final String KIND_EXAMPLE_CORRECT_ID_RES = "/kind-schema/kind-test-example-correct-id";
	private static final String KIND_EXAMPLE_GARBAGE_ID_RES = "/kind-schema/kind-test-example-garbage-id";

	@Test
	public void kind_canReadBack() throws Exception
	{
		manage.kind("my-kind").putJsonResource(KIND_SCHEMA_RES);

		final ApiKind kind = manage.kind("my-kind").getKind();

		assertThat(kind.level(), is(KindLevel.entity));
		assertThat(kind.java().javaKind(), is(JavaKind.INTERFACE));
		assertThat(kind.kindId(), is("my-kind"));
		assertThat(kind.instancePresentation().iconUrl(), is("http://example.com/icon.png"));
		assertThat(kind.instancePresentation().openIconUrl(), is("http://example.com/icon2.png"));
		assertFalse("This kind not part of system", kind.partOfSystem());
	}

	@Test
	public void kind_canBeTaggedAsSystem() throws Exception
	{
		manage.kind("my-kind").putJsonResource(KIND_SCHEMA_SYSTEM_RES);

		final ApiKind kind = manage.kind("my-kind").getKind();

		assertTrue("This kind should be part of system", kind.partOfSystem());
	}

	@Test
	public void kind_isListed() throws Exception
	{
		final ManagementPathKind kind = manage.kind("my-kind");
		kind.putJsonResource(KIND_SCHEMA_RES);

		final List<ListKindItem> list = manage.listKinds().childResources();

		final ListKindItem item = list.stream().filter(k -> k.url().equals(kind.url())).findFirst().get();
		assertThat(item.url(), is(kind.url()));
		assertThat(item.instancePresentation().iconUrl(), is("http://example.com/icon.png"));
	}

	@Test
	public void kind_withWrongId_rejected() throws Exception
	{
		final ResponseType response1 = manage.kind("my-kind").putJsonResourceResponseType(KIND_EXAMPLE_GARBAGE_ID_RES);
		final ResponseType response2 = manage.kind("my-kind").putJsonResourceResponseType(KIND_EXAMPLE_CORRECT_ID_RES);

		assertThat(response1, is(ResponseType.BAD_REQUEST));
		assertThat(response2, is(ResponseType.NO_CONTENT));
	}
}
