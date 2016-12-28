package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

final class MutableByKeyImpl<K, T> implements MutableByKey<K, T>
{
	private final MutableListOperations<T> base;
	private final Function<T, K> keyGetter;

	private MutableByKeyImpl(final MutableListOperations<T> base, final Function<T, K> keyGetter)
	{
		this.base = checkNotNull(base);
		this.keyGetter = checkNotNull(keyGetter);
	}

	static <K, T> MutableByKey<K, T> of(final MutableListOperations<T> base, final Function<T, K> keyGetter)
	{
		return new MutableByKeyImpl<>(base, keyGetter);
	}

	@Override
	public OptView<T> optGet(final K key)
	{
		checkNotNull(key);
		return base.list().stream()
				.filter(x -> key.equals(keyGetter.apply(x)))
				.collect(OtherCollectors.toOpt());
	}

	@Override
	public T getWithCreator(final K key, final Function<K, T> creator)
	{
		checkNotNull(key);
		final OptView<T> result = optGet(key);
		if (result.isPresent())
		{
			return result.get();
		}
		else
		{
			final T newItem = creator.apply(key);
			final K returnedName = keyGetter.apply(newItem);
			if (!key.equals(returnedName))
			{
				throw new IllegalStateException(
						"Creator created something with the wrong key! Should be " + key + ", was " + returnedName);
			}
			base.insertAtEnd(newItem);
			return newItem;
		}
	}

	@Override
	public MutableListView<T> getAll(final K key)
	{
		return list().filter(x -> key.equals(keyGetter.apply(x)));
	}

	@Override
	public MutableListView<T> list()
	{
		return MutableListViewImpl.of(base);
	}

	@Override
	public Embedded<T> getEmbedded(final K key)
	{
		return EmbeddedImpl.of(this, key);
	}

	@Override
	public void deleteByKey(final K key)
	{
		base.removeIf(x -> key.equals(keyGetter.apply(x)));
	}

	@Override
	public void put(final K key, final T value)
	{
		checkNotNull(key);
		if (!key.equals(keyGetter.apply(value)))
		{
			throw new IllegalArgumentException(
					"MutableByKeyImpl.put: key is wrong. " + key + " vs " + keyGetter.apply(value));
		}
		final AtomicBoolean b = new AtomicBoolean(false);
		base.transform(x -> {
			if (key.equals(keyGetter.apply(x)))
			{
				b.set(true);
				return value;
			}
			else
			{
				return x;
			}
		});
		if (!b.get())
		{
			base.insertAtEnd(value);
		}
	}

	@Override
	public ListView<K> keys()
	{
		return base.list().map(keyGetter);
	}
}
