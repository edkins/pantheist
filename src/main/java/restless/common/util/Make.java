package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Operations on immutable objects.
 */
public class Make
{
	private Make()
	{
		throw new UnsupportedOperationException();
	}

	public static <T> MakeList<T, List<T>> list()
	{
		return Make.<List<T>>single().through(xs -> xs);
	}

	public static <T, R> MakeList<T, R> wrappedList(final Function<List<T>, R> func)
	{
		return Make.<R>single().through(func);
	}

	public static <T> MakeSingle<T, T> single()
	{
		return MakeSingleImpl.fromFunc(x -> x);
	}

	public static <T> MakeList<T, Optional<T>> failIfMultiple()
	{
		return Make.<Optional<T>>single().fromList(Optional::empty, Optional::of, xs -> {
			throw new IllegalStateException("failIfMultiple: expecting 1 element, got " + xs.size());
		});
	}

	public static <T> MakeList<T, T> theOnly()
	{
		return Make.<T>single().fromList(() -> {
			throw new NoSuchElementException("theOnly: nothing found");
		}, x -> x, xs -> {
			throw new IllegalStateException("theOnly: expecting 1 element, got " + xs.size());
		});
	}

	public static <T> T last(final List<T> xs)
	{
		checkNotNull(xs);
		if (xs.isEmpty())
		{
			throw new IllegalArgumentException("last: empty list");
		}
		return xs.get(xs.size() - 1);
	}
}
