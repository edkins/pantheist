package restless.handler.filesystem.backend;

import restless.handler.binding.backend.ManagementFunctions;

interface FilesystemFactory
{
	LockedFile lockedFile(FsPath path);

	ManagementFunctions managementFunctions(FsPath path);
}
