package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * Operations on immutable objects.
 */
public class Make
{
	private Make()
	{
		throw new UnsupportedOperationException();
	}

	public static <T> ImmutableList<T> list(final List<T> xs, final T x)
	{
		return ImmutableList.<T>builder().addAll(xs).add(x).build();
	}

	public static <T> ImmutableList<T> listWithout(final List<T> xs, final Predicate<T> dropPredicate)
	{
		checkNotNull(xs);
		checkNotNull(dropPredicate);
		return ImmutableList.copyOf(xs.stream().filter(x -> !dropPredicate.test(x)).collect(Collectors.toList()));
	}

	public static <T> ImmutableList<T> listWithSingleItemRemoved(final List<T> xs, final T dropX)
	{
		checkNotNull(xs);
		checkNotNull(dropX);
		final Builder<T> builder = ImmutableList.<T>builder();
		boolean found = false;
		for (final T x : xs)
		{
			if (x.equals(dropX))
			{
				if (found)
				{
					throw new IllegalStateException("Multiple copies of item");
				}
				found = true;
			}
			else
			{
				builder.add(x);
			}
		}
		if (!found)
		{
			throw new IllegalStateException("Item not found in order to remove it");
		}
		return builder.build();
	}

	public static <T> ImmutableList<T> tail(final List<T> xs)
	{
		checkNotNull(xs);
		if (xs.isEmpty())
		{
			throw new IllegalArgumentException("tail: empty list");
		}
		return ImmutableList.copyOf(xs.subList(1, xs.size()));
	}

	public static <T> ImmutableList<T> init(final List<T> xs)
	{
		checkNotNull(xs);
		if (xs.isEmpty())
		{
			throw new IllegalArgumentException("init: empty list");
		}
		return ImmutableList.copyOf(xs.subList(0, xs.size() - 1));
	}

	public static <K, V> ImmutableMap<K, V> map(final Map<K, V> map, final K key, final V value)
	{
		checkNotNull(map);
		checkNotNull(key);
		checkNotNull(value);
		if (map.containsKey(key))
		{
			throw new IllegalStateException("Key already in map: " + key);
		}
		return ImmutableMap.<K, V>builder().putAll(map).put(key, value).build();
	}

	public static <K, V> ImmutableMap<K, V> overrideMapIfPresent(final Map<K, V> map, final K key, final V value)
	{
		checkNotNull(map);
		checkNotNull(key);
		checkNotNull(value);
		final Map<K, V> hashMap = new HashMap<>(map);
		hashMap.put(key, value);
		return ImmutableMap.copyOf(hashMap);
	}

	public static <K, V> ImmutableMap<K, V> overrideMap(final Map<K, V> map, final K key, final V value)
	{
		checkNotNull(map);
		checkNotNull(key);
		checkNotNull(value);
		if (!map.containsKey(key))
		{
			throw new IllegalStateException("Key not in map: " + key);
		}
		final Map<K, V> hashMap = new HashMap<>(map);
		hashMap.put(key, value);
		return ImmutableMap.copyOf(hashMap);
	}

	/**
	 * Return a map without the entries whose values satisfy dropPredicate
	 */
	public static <K, V> ImmutableMap<K, V> mapWithout(final Map<K, V> map, final Predicate<V> dropPredicate)
	{
		checkNotNull(map);
		checkNotNull(dropPredicate);
		final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		for (final Entry<K, V> entry : map.entrySet())
		{
			if (!dropPredicate.test(entry.getValue()))
			{
				builder.put(entry);
			}
		}
		return builder.build();
	}

	public static <K, V> ImmutableMap<String, V> keysIntoStrings(final Map<K, V> map)
	{
		checkNotNull(map);
		final ImmutableMap.Builder<String, V> builder = ImmutableMap.builder();
		for (final Entry<K, V> entry : map.entrySet())
		{
			builder.put(entry.getKey().toString(), entry.getValue());
		}
		return builder.build();
	}

	public static <K, V> ImmutableMap<K, V> keysOutOfStrings(final Map<String, V> map,
			final Function<String, K> fromString)
	{
		checkNotNull(map);
		final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
		for (final Entry<String, V> entry : map.entrySet())
		{
			builder.put(fromString.apply(entry.getKey()), entry.getValue());
		}
		return builder.build();
	}

	public static String spaces(final int count)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++)
		{
			sb.append(' ');
		}
		return sb.toString();
	}
}
