package restless.handler.binding.model;

import javax.inject.Inject;

import restless.handler.filesystem.backend.FsPath;

final class HandlerEmptyImpl implements Handler
{
	@Inject
	private HandlerEmptyImpl()
	{

	}

	@Override
	public HandlerType type()
	{
		return HandlerType.empty;
	}

	@Override
	public FsPath filesystemBucket()
	{
		throw new UnsupportedOperationException();
	}

}
