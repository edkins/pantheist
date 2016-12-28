package restless.common.util;

public interface ByKey<K, T>
{
	/**
	 * Get a single item by key.
	 *
	 * @throws IllegalStateException if there are multiple items with this key
	 */
	OptView<T> optGet(K key);

	/**
	 * Gets all items with the given key.
	 */
	ListView<T> getAll(K key);

	/**
	 * List everything.
	 */
	ListView<T> list();
}
