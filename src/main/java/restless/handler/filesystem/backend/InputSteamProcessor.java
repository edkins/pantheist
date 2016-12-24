package restless.handler.filesystem.backend;

import java.io.IOException;
import java.io.InputStream;

public interface InputSteamProcessor<T>
{
	T process(InputStream input) throws IOException;
}
