package restless.handler.filesystem.backend;

import restless.handler.binding.backend.ManagementFunctions;

interface FilesystemFactory
{
	FilesystemSnapshot snapshot();

	ManagementFunctions managementFunctions(FsPath path);
}
