package restless.common.util;

import java.util.Iterator;
import java.util.function.Consumer;

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

	public static <T> void run(final Consumer<T> fn, final Iterator<T> iterator)
	{
		if (iterator.hasNext())
		{
			try
			{
				fn.accept(iterator.next());
			}
			finally
			{
				run(fn, iterator);
			}
		}
	}

	public static void run(final Runnable... tasks)
	{
		run(Runnable::run, ImmutableList.copyOf(tasks).iterator());
	}
}
