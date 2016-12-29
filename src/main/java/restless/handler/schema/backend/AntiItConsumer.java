package restless.handler.schema.backend;

public interface AntiItConsumer<T>
{
	boolean wantMore(T item);
}
