package restless.common.util;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

final class EmptyImpl<T> implements ImmutableOpt<T>, ListView<T>
{
	private EmptyImpl()
	{
	}

	static <T> EmptyImpl<T> empty()
	{
		return new EmptyImpl<>();
	}

	@Override
	public <U> EmptyImpl<U> map(final Function<T, U> fn)
	{
		return empty();
	}

	@Override
	public <U> EmptyImpl<U> optMap(final Function<T, ? extends OptView<U>> fn)
	{
		return empty();
	}

	@Override
	public EmptyImpl<T> filter(final Predicate<T> predicate)
	{
		return this;
	}

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public T get()
	{
		throw new NoSuchElementException("Empty");
	}

	@Override
	public ImmutableOpt<T> immutableCopy()
	{
		return this;
	}

	@Override
	public T orElse(final Supplier<T> supplier)
	{
		return supplier.get();
	}

	@Override
	public <U> ListView<U> flatMap(final Function<T, ? extends ListView<U>> fn)
	{
		return View.emptyList();
	}

	@Override
	public boolean hasValue(final T expectedValue)
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasSequenceOfItems(final T... items)
	{
		return items.length == 0;
	}

	@Override
	public Stream<T> stream()
	{
		return Stream.empty();
	}

	@Override
	public List<T> toList()
	{
		return ImmutableList.of();
	}

	@Override
	public OptView<T> failIfMultiple()
	{
		return this;
	}

	@Override
	public void forEach(final Consumer<T> consumer)
	{
		// no-op
	}

}
