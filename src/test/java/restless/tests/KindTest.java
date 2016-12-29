package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

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
}
