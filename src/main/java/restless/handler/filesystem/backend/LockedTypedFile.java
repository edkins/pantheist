package restless.handler.filesystem.backend;

public interface LockedTypedFile<T> extends AutoCloseable
{
	boolean fileExits();

	T read();

	void write(T value);

	@Override
	void close();
}
