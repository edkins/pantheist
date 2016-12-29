package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Pair<T, U>
{
	private final T first;
	private final U second;

	private Pair(final T first, final U second)
	{
		this.first = checkNotNull(first);
		this.second = checkNotNull(second);
	}

	public static <T, U> Pair<T, U> of(final T first, final U second)
	{
		return new Pair<>(first, second);
	}

	public T first()
	{
		return first;
	}

	public U second()
	{
		return second;
	}
}
