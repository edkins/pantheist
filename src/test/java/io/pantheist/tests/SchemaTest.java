package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import io.pantheist.api.schema.model.ListSchemaItem;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ManagementPathSchema;
import io.pantheist.testclient.api.ResponseType;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class SchemaTest
{
	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;
	private ManagementPathSchema schema;

	private static final String JSON_SCHEMA_MIME = "application/schema+json";
	private static final String JSON_SCHEMA_COFFEE_RES = "/json-schema/coffee";
	private static final String JSON_SCHEMA_COFFEE_WITH_ID_RES = "/json-schema/coffee-with-id";
	private static final String JSON_SCHEMA_INTLIST_RES = "/json-schema/nonempty_nonnegative_int_list";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
		schema = mainRule.actions().manage().jsonSchema("coffee");
	}

	@Test
	public void schema_canReadItBack() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);

		final String data = schema.getJsonSchemaString();

		JSONAssert.assertEquals(data, mainRule.resource(JSON_SCHEMA_COFFEE_RES), true);
	}

	@Test
	public void schema_kindUrl() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);

		assertThat(schema.headKindUrl(), is(manage.kind("json-schema").url()));
	}

	@Test
	public void schema_canList() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);

		final List<ListSchemaItem> list = manage.listJsonSchemas().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(schema.url()));
	}

	@Test
	public void schema_canPutTwice() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);
		schema.putJsonSchemaResource(JSON_SCHEMA_INTLIST_RES);
	}

	@Test
	public void schema_canDelete() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);

		assertThat(schema.getJsonSchemaResponseType(), is(ResponseType.OK));

		schema.delete();

		assertThat(schema.getJsonSchemaResponseType(), is(ResponseType.NOT_FOUND));
	}

	@Test
	public void schema_ifMissing_deleteReturnsNotFound() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);

		assertThat(schema.deleteResponseType(), is(ResponseType.NO_CONTENT));
	}

	@Test
	public void invalidSchema_rejected() throws Exception
	{
		final ResponseType responseType = schema.putJsonSchemaResourceResponseType("/json-schema/invalid");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

	@Test
	public void schema_validData_allowed() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);
		final ResponseType responseType = schema.validate(mainRule.resource("/json-example/coffee-valid"),
				"application/json");
		assertEquals(ResponseType.NO_CONTENT, responseType);
	}

	@Test
	public void schema_invalidData_notAllowed() throws Exception
	{
		schema.putJsonSchemaResource(JSON_SCHEMA_COFFEE_RES);
		final ResponseType responseType = schema.validate(mainRule.resource("/json-example/coffee-invalid"),
				"application/json");
		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

	@Test
	public void schema_canPost_andReadItBack() throws Exception
	{
		// We don't know ahead of time what the port will be, so need to substitute it here
		// in order to provide a valid url for the id.
		final String originalText = mainRule.resource(JSON_SCHEMA_COFFEE_WITH_ID_RES)
				.replace("{{PORT}}", String.valueOf(mainRule.nginxPort()));

		final String url = manage.kind("json-schema").postCreate(originalText, JSON_SCHEMA_MIME);

		final ManagementPathSchema newSchema = manage.jsonSchema("coffee-with-id");

		assertThat(url, is(newSchema.url()));

		final String data = newSchema.getJsonSchemaString();

		JSONAssert.assertEquals(data, originalText, true);
	}

	@Test
	public void schema_postTwice_samePlace_secondOneFails() throws Exception
	{
		// We don't know ahead of time what the port will be, so need to substitute it here
		// in order to provide a valid url for the id.
		final String originalText = mainRule.resource(JSON_SCHEMA_COFFEE_WITH_ID_RES)
				.replace("{{PORT}}", String.valueOf(mainRule.nginxPort()));

		final ResponseType response1 = manage.kind("json-schema").postCreateResponseType(originalText,
				JSON_SCHEMA_MIME);
		final ResponseType response2 = manage.kind("json-schema").postCreateResponseType(originalText,
				JSON_SCHEMA_MIME);

		assertThat(response1, is(ResponseType.CREATED));
		assertThat(response2, is(ResponseType.CONFLICT));
	}
}
