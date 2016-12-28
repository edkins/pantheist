package restless.common.util;

public interface Embedded<T>
{
	OptView<T> opt();

	void delete();

	void supply(T newValue);

	void setSingle(T newValue);
}
