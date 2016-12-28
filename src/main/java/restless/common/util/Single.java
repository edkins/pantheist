package restless.common.util;

import java.util.function.Function;

interface Single<T> extends ImmutableOpt<T>, Possible<T>
{
	@Override
	<U> Single<U> map(Function<T, U> fn);
}
