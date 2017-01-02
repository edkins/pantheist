package io.pantheist.system.initializer;

public interface Initializer extends AutoCloseable
{
	void start();

	@Override
	void close();

	/**
	 * Start another thread which will schedule shutdown.
	 */
	void stopAsync();

	void reload();

	void regenerateDb();
}
