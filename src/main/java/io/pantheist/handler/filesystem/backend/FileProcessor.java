package io.pantheist.handler.filesystem.backend;

import java.io.IOException;

public interface FileProcessor
{
	void process(FsPathMapping map) throws IOException;
}
