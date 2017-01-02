package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class SubKindTest
{
	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;

	private static final String JAVA_BUTTER_NAME = "WithButterAnnotation";
	private static final String JAVA_SUGAR_NAME = "WithSugar";
	private static final String JAVA_BUTTER_SUGAR_NAME = "WithButterSugar";
	private static final String JAVA_BUTTER_SUGAR_CHERRY_NAME = "WithButterSugarCherry";
	private static final String JAVA_FILE = "java-file";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
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

		assertThat(javaButter.describeJavaFile().kindUrl(), is(butterKind.url()));
		assertThat(javaSugar.describeJavaFile().kindUrl(), is(baseKind.url()));
		assertThat(javaButterSugar.describeJavaFile().kindUrl(), is(butterSugarKind.url()));
	}

	@Test
	public void multipleInheritance_needsToMatchBoth_andSelf() throws Exception
	{
		final ManagementPathKind butterKind = mainRule.putKindResource("java-interface-with-butter-annotation");
		final ManagementPathKind sugarKind = mainRule.putKindResource("java-interface-sugar");
		final ManagementPathKind butterSugarCherryKind = mainRule.putKindResource("java-interface-butter-sugar-cherry");
		final ManagementPathJavaFile javaButter = mainRule.putJavaResource(JAVA_BUTTER_NAME);
		final ManagementPathJavaFile javaSugar = mainRule.putJavaResource(JAVA_SUGAR_NAME);
		final ManagementPathJavaFile javaButterSugar = mainRule.putJavaResource(JAVA_BUTTER_SUGAR_NAME);
		final ManagementPathJavaFile javaButterSugarCherry = mainRule.putJavaResource(JAVA_BUTTER_SUGAR_CHERRY_NAME);

		assertThat(javaButter.describeJavaFile().kindUrl(), is(butterKind.url()));
		assertThat(javaSugar.describeJavaFile().kindUrl(), is(sugarKind.url()));
		assertThat(javaButterSugar.describeJavaFile().kindUrl(), isOneOf(butterKind.url(), sugarKind.url())); // should match one or the other but I don't really care which here
		assertThat(javaButterSugarCherry.describeJavaFile().kindUrl(), is(butterSugarCherryKind.url()));
	}
}
