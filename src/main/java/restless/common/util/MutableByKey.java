package restless.common.util;

import java.util.function.Function;

public interface MutableByKey<K, T>
{
	T getWithCreator(K key, Function<K, T> creator);

	MutableListView<T> list();

	Embedded<T> getEmbedded(K key);

	void deleteByKey(K key);

	MutableListView<T> getAll(K key);

	void put(K key, T value);

	ListView<K> keys();

	OptView<T> optGet(K key);
}
