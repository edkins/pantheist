package restless.handler.nginx.parser;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;

public class HandlerNginxParserModuleTest
{
	private NginxSyntax sut;

	@Before
	public void setup()
	{
		sut = Guice.createInjector(new HandlerNginxParserModule()).getInstance(NginxSyntax.class);
	}

	@Test
	public void validConfigurations_canParse_andTurnBackToOriginal() throws Exception
	{
		canParse("my-example");
		canParse("example-with-leading-whitespace");
	}

	private void canParse(final String path) throws IOException
	{
		final String text = resource("/nginx-conf/" + path);
		final NginxRoot parseTree = sut.parse(text);

		assertThat(parseTree.toString(), is(text));
	}

	private String resource(final String path) throws IOException
	{
		try (InputStream inputStream = HandlerNginxParserModuleTest.class.getResourceAsStream(path))
		{
			assertNotNull("Resource does not exist: " + path, inputStream);
			return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		}
	}
}
