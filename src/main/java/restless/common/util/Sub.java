package restless.common.util;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Sub
{
	private Sub()
	{
		throw new UnsupportedOperationException();
	}

	public static <T> SubIterator<T> concat(final SubIterator<T> xs, final SubIterator<T> ys)
	{
		return new SubIterator<T>() {
			@Override
			public boolean tryAdvance(final Consumer<? super T> action)
			{
				if (xs.tryAdvance(action))
				{
					return true;
				}
				else
				{
					return ys.tryAdvance(action);
				}
			}

		};
	}

	public static <T> SubIterator<T> empty()
	{
		return new SubIterator<T>() {
			@Override
			public boolean tryAdvance(final Consumer<? super T> action)
			{
				return false;
			}
		};
	}

	public static <T> SubIterator<T> single(final T item)
	{
		return new SubIterator<T>() {
			boolean exists = true;

			@Override
			public boolean tryAdvance(final Consumer<? super T> action)
			{
				if (exists)
				{
					action.accept(item);
					exists = false;
					return true;
				}
				else
				{
					return false;
				}
			}
		};
	}

	public static <T> SubIterator<T> from(final List<T> list)
	{
		return from(list.stream().spliterator());
	}

	public static <T> SubIterator<T> from(final Spliterator<T> spliterator)
	{
		return new SubIterator<T>() {
			@Override
			public boolean tryAdvance(final Consumer<? super T> action)
			{
				return spliterator.tryAdvance(action);
			}
		};
	}

	public static <T> SubIterator<T> filter(final SubIterator<T> xs, final Predicate<T> predicate)
	{
		return new SubIterator<T>() {
			private T next = null;
			private boolean decision = false;

			private final Consumer<? super T> setNext = (x) -> {
				next = x;
				decision = predicate.test(x);
			};

			@Override
			public boolean tryAdvance(final Consumer<? super T> action)
			{
				while (xs.tryAdvance(setNext))
				{
					if (decision)
					{
						action.accept(next);
						return true;
					}
				}
				return false;
			}
		};
	}

	public static <T> void forEachRemaining(final SubIterator<T> xs, final Consumer<T> consumer)
	{
		while (xs.tryAdvance(consumer))
		{

		}
	}
}
