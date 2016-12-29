package restless.common.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MakeSingle<T, R>
{
	<U> MakeList<U, R> fromList(Supplier<T> empty, Function<U, T> single, Function<List<U>, T> multiple);

	<U> MakeList<U, R> through(Function<List<U>, T> func);

	<U> MakeList<U, R> throughAntiIterator(Function<AntiIterator<U>, T> func);

	<U> MakeList<U, R> snowball(T initial, BiFunction<T, U, T> accumulate);

	R of(T x);
}
