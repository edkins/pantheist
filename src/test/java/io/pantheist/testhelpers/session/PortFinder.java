package io.pantheist.testhelpers.session;

import java.io.IOException;
import java.net.ServerSocket;

import com.google.common.base.Throwables;

import io.pantheist.common.util.MutableOpt;
import io.pantheist.common.util.View;

final class PortFinder
{
	private final MutableOpt<Integer> value;

	private PortFinder()
	{
		value = View.mutableOpt();
	}

	static PortFinder empty()
	{
		return new PortFinder();
	}

	public synchronized int get()
	{
		if (!value.isPresent())
		{
			value.supply(findFreePort());
		}
		return value.get();
	}

	public synchronized void clear()
	{
		value.clear();
	}

	private int findFreePort()
	{
		try (ServerSocket socket = new ServerSocket(0))
		{
			return socket.getLocalPort();
		}
		catch (final IOException e)
		{
			throw Throwables.propagate(e);
		}
	}
}
