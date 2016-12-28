package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

final class EmbeddedImpl<K, T> implements Embedded<T>
{
	private final MutableByKey<K, T> parent;
	private final K key;

	private EmbeddedImpl(final MutableByKey<K, T> parent, final K key)
	{
		this.parent = checkNotNull(parent);
		this.key = checkNotNull(key);
	}

	static <K, T> Embedded<T> of(final MutableByKey<K, T> parent, final K key)
	{
		return new EmbeddedImpl<>(parent, key);
	}

	@Override
	public void delete()
	{
		if (!opt().isPresent())
		{
			throw new IllegalStateException("delete: embedded not present");
		}
		parent.deleteByKey(key);
	}

	@Override
	public void supply(final T newValue)
	{
		if (opt().isPresent())
		{
			throw new IllegalStateException("supply: embedded already present");
		}
		parent.put(key, newValue);
	}

	@Override
	public OptView<T> opt()
	{
		return parent.optGet(key);
	}

	@Override
	public void setSingle(final T newValue)
	{
		parent.put(key, newValue);
	}

}
