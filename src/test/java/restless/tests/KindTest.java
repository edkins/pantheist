package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import restless.api.management.model.ApiEntity;
import restless.client.api.ManagementData;
import restless.client.api.ManagementPathKind;
import restless.handler.kind.model.JavaKind;
import restless.handler.kind.model.Kind;
import restless.handler.kind.model.KindLevel;

public class KindTest extends BaseTest
{
	@Test
	public void kind_canReadBack() throws Exception
	{
		manage.kind("my-kind").putJsonResource("/kind-schema/pojo");

		final Kind kind = manage.kind("my-kind").getKind();

		assertThat(kind.level(), is(KindLevel.entity));
		assertThat(kind.java().required(), is(true));
		assertThat(kind.java().javaKind(), is(JavaKind.INTERFACE));
	}

	@Test
	public void entity_withKind_isValid() throws Exception
	{
		final ManagementData java = manage.javaPackage("restless.examples").file("NonEmptyNonNegativeIntList");
		java.putResource("/java-example/NonEmptyNonNegativeIntList", "text/plain");

		final ManagementPathKind kindPath = manage.kind("my-kind");
		kindPath.putJsonResource("/kind-schema/pojo");

		manage.entity("my-entity").putEntity(kindPath.url(), null, java.url());

		final ApiEntity result = manage.entity("my-entity").getEntity();
		assertThat(result.kindUrl(), is(kindPath.url()));
		assertTrue("Entity should be valid", result.valid());
	}
}
