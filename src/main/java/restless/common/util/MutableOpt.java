package restless.common.util;

public interface MutableOpt<T> extends OptView<T>
{

	void supply(T newValue);

	void supplyOpt(OptView<T> other);

	void clear();

	void setSingle(T item);
}
