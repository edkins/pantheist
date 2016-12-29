package restless.handler.schema.backend;

/**
 * An iterator which calls you, and you tell it when to stop sending things.
 */
public interface AntiIterator<T>
{
	void process(AntiItConsumer<T> consumer);
}
