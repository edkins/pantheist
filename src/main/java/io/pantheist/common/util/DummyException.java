package io.pantheist.common.util;

/**
 * Never actually thrown.
 *
 * Just used by helper methods that always throw exceptions, and need to return
 * something so that you can 'throw' them and keep the compiler happy.
 */
public class DummyException extends RuntimeException
{
	private static final long serialVersionUID = 7291054215333495778L;

	private DummyException()
	{
		throw new UnsupportedOperationException();
	}
}
