package restless.common.util;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Used to execute several cleanup tasks in sequence, any one of which might throw
 * a RuntimeException. If they do, the remaining tasks will still be executed but
 * the exception will get propagated at the end.
 */
public final class Cleanup
{
	private Cleanup()
	{
		throw new UnsupportedOperationException();
	}

	private static void run(final List<Runnable> tasks)
	{
		if (!tasks.isEmpty())
		{
			try
			{
				tasks.get(0).run();
			}
			finally
			{
				run(Make.tail(tasks));
			}
		}
	}

	public static void run(final Runnable... tasks)
	{
		run(ImmutableList.copyOf(tasks));
	}
}
