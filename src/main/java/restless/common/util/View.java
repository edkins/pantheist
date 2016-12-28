package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

public final class View
{
	private View()
	{
		throw new IllegalArgumentException();
	}

	public static <T> MutableOpt<T> mutableOpt()
	{
		return MutableOptImpl.empty();
	}

	public static <T> Possible<T> ok(final T value)
	{
		checkNotNull(value);
		return PossibleOkImpl.of(value);
	}

	public static Possible<Void> noContent()
	{
		return PossibleOkImpl.of(null);
	}
}
