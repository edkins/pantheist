package restless.handler.filesystem.backend;

import java.io.File;
import java.io.IOException;

public interface SingleFileProcessor
{
	void process(File file) throws IOException;
}
