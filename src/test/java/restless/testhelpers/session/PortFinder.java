package restless.testhelpers.session;

import java.io.IOException;
import java.net.ServerSocket;

import com.google.common.base.Throwables;

import restless.common.util.MutableOptional;

final class PortFinder
{
	private final MutableOptional<Integer> value;

	private PortFinder()
	{
		value = MutableOptional.empty();
	}

	static PortFinder empty()
	{
		return new PortFinder();
	}

	public synchronized int get()
	{
		if (!value.isPresent())
		{
			value.add(findFreePort());
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
