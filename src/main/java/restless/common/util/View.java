package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

public final class View
{
	private View()
	{
		throw new IllegalArgumentException();
	}

	public static <T> ListView<T> list(final List<T> list)
	{
		return ListViewImpl.of(list);
	}

	public static <T> MutableListView<T> mutableCopy(final List<T> list)
	{
		return MutableListViewImpl.of(ListViewImpl.of(list));
	}

	public static <T> ImmutableOpt<T> single(final T value)
	{
		checkNotNull(value);
		return SingletonImpl.of(value);
	}

	public static <T> ImmutableOpt<T> empty()
	{
		return EmptyImpl.empty();
	}

	public static <T> ListView<T> emptyList()
	{
		return EmptyImpl.empty();
	}

	public static <T> MutableOpt<T> mutableOpt()
	{
		return MutableOptImpl.empty();
	}

	public static <T> ImmutableOpt<T> nullable(@Nullable final T value)
	{
		if (value == null)
		{
			return empty();
		}
		else
		{
			return single(value);
		}
	}

	public static <T> Possible<T> ok(final T value)
	{
		checkNotNull(value);
		return SingletonImpl.of(value);
	}

	public static Possible<Void> noContent()
	{
		return SingletonImpl.of(null);
	}
}
