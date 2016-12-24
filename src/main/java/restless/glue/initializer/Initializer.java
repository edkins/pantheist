package restless.glue.initializer;

public interface Initializer extends AutoCloseable
{
	void start();

	@Override
	void close();
}
