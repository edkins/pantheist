package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ListViewImpl<T> implements ListView<T>, MutableListOperations<T>
{
	private final List<T> list;

	private ListViewImpl(final List<T> list)
	{
		this.list = checkNotNull(list);
	}

	static <T> ListViewImpl<T> of(final List<T> list)
	{
		return new ListViewImpl<>(list);
	}

	@Override
	public void insertAtEnd(final T item)
	{
		list.add(item);
	}

	@Override
	public <U> ListView<U> map(final Function<T, U> fn)
	{
		return View.list(list.stream().map(fn).collect(Collectors.toList()));
	}

	@Override
	public List<T> toList()
	{
		return list;
	}

	@Override
	public ListView<T> filter(final Predicate<T> predicate)
	{
		return View.list(list.stream().filter(predicate).collect(Collectors.toList()));
	}

	@Override
	public OptView<T> failIfMultiple()
	{
		if (list.size() == 0)
		{
			return View.empty();
		}
		else if (list.size() == 1)
		{
			return View.single(list.get(0));
		}
		else
		{
			throw new IllegalStateException("Multiple items in list");
		}
	}

	@Override
	public <U> ListView<U> optMap(final Function<T, ? extends OptView<U>> fn)
	{
		return View.list(list.stream()
				.map(fn)
				.filter(OptView::isPresent)
				.map(OptView::get)
				.collect(Collectors.toList()));
	}

	@Override
	public <U> ListView<U> flatMap(final Function<T, ? extends ListView<U>> fn)
	{
		return View.list(list.stream()
				.<U>flatMap(x -> fn.apply(x).stream())
				.collect(Collectors.toList()));
	}

	@Override
	public void forEach(final Consumer<T> consumer)
	{
		list.forEach(consumer);
	}

	@Override
	public Stream<T> stream()
	{
		return list.stream();
	}

	/*
		@Override
		public <K> ByKey<K, T> organizeByKey(final Function<T, K> keyGetter)
		{
			return MutableByKeyImpl.of(this, keyGetter);
		}
	*/
	@SuppressWarnings("unchecked")
	@Override
	public boolean hasSequenceOfItems(final T... items)
	{
		final Iterator<T> iterator = stream().iterator();
		for (int i = 0; i < items.length; i++)
		{
			if (!iterator.hasNext())
			{
				return false;
			}
			if (!iterator.next().equals(items[i]))
			{
				return false;
			}
		}
		return iterator.hasNext();
	}

	@Override
	public void removeIf(final Predicate<T> predicate)
	{
		list.removeIf(predicate);
	}

	@Override
	public ListView<T> list()
	{
		return this;
	}

	@Override
	public void transform(final Function<T, T> fn)
	{
		final ListIterator<T> iterator = list.listIterator();
		while (iterator.hasNext())
		{
			final T item = iterator.next();
			iterator.set(fn.apply(item));
		}
	}

}
