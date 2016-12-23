package restless.handler.nginx.model;

import java.io.File;

import org.junit.Test;

public class NginxConfigImplTest
{
	@Test
	public void testName() throws Exception
	{
		final NginxConfig sut = new NginxConfigImpl();

		sut.pid().giveValue("/home/giles/junk/thing.pid");
		sut.error_log().giveValue("/home/giles/junk/whatever.txt");
		sut.http().root().giveFile(new File("foo"));
		sut.http().access_log().giveValue("/home/giles/junk/whatever.txt");
		final NginxServer server = sut.http().addServer("127.0.0.1", 8091);
		final NginxLocation location = server.addLocation("/");

		System.out.println(sut);
	}
}
