package restless.system.initializer;

public interface Initializer extends AutoCloseable
{
	void start();

	@Override
	void close();
}
