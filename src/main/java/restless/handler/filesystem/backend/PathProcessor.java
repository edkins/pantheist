package restless.handler.filesystem.backend;

import java.io.File;
import java.io.IOException;

public interface PathProcessor
{
	void process(FsPath path, File file) throws IOException;
}
