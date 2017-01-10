package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

final class GraphBuilderImpl implements GraphBuilder
{
	private final EventFabric eventFabric;

	GraphBuilderImpl(final EventFabric eventFabric)
	{
		this.eventFabric = checkNotNull(eventFabric);
	}

	@Override
	public <T> Dep<T> singleton(final T object)
	{
		final Dep<T> provider = SingletonProvider.of(object);

		eventFabric.connect(provider, ImmutableList.of());

		return provider;
	}

	@Override
	public <T> MutableProvider<T> mutable(final T initialValue)
	{
		final EventOriginImpl origin = new EventOriginImpl();
		final MutableProvider<T> provider = MutableProviderImpl.from(initialValue, origin);
		eventFabric.connect(provider, ImmutableList.of());
		origin.setNode(provider);
		return provider;
	}

	@Override
	public <T, U> Dep<U> oblivious(final Function<T, U> fn, final Dep<T> dep)
	{
		final Dep<U> provider = FunctionalProvider.from(fn, dep);

		eventFabric.connect(provider, ImmutableList.of(dep));

		return provider;
	}

	@Override
	public <T, U, R> Dep<R> oblivious2(final BiFunction<T, U, R> fn, final Dep<T> dep1,
			final Dep<U> dep2)
	{
		final Dep<R> provider = BifunctionalProvider.from(fn, dep1, dep2);

		eventFabric.connect(provider, ImmutableList.of(dep1, dep2));

		return provider;
	}

	private final class EventOriginImpl implements EventOrigin
	{
		private EventNode node = null;

		private void setNode(final EventNode node)
		{
			if (this.node != null)
			{
				throw new IllegalStateException("Node already set");
			}
			this.node = node;
		}

		@Override
		public void fire()
		{
			if (this.node == null)
			{
				throw new IllegalStateException("Node not set yet");
			}
			eventFabric.signal(node, true);
		}

	}

	@Override
	public <T> Dep<T> absorb(final Dep<T> dep)
	{
		final Dep<T> provider = Absorber.of(dep);

		eventFabric.connect(provider, ImmutableList.of(dep));

		return provider;
	}

	@Override
	public <T> Dep<T> absorbEqual(final Dep<T> dep)
	{
		final Dep<T> provider = AbsorbEqual.of(dep);

		eventFabric.connect(provider, ImmutableList.of(dep));

		return provider;
	}

	@Override
	public <T> Dep<Supplier<T>> supplier(final Dep<T> dep)
	{
		final Dep<Supplier<T>> provider = SupplierSupplier.of(dep);

		eventFabric.connect(provider, ImmutableList.of(dep));

		return provider;
	}

	@Override
	public <T, U> Dep<U> install(final Function<T, NotifiableSupplier<U>> constructor, final Dep<T> dep)
	{
		final Dep<U> provider = AdaptorDep.of(constructor, dep);

		eventFabric.connect(provider, ImmutableList.of(dep));

		return provider;
	}

	@Override
	public Dep<EventOrigin> eventSource()
	{
		final Dep<EventOrigin> provider = EventSource.in(eventFabric);
		eventFabric.connect(provider, ImmutableList.of());
		return provider;
	}

	@Override
	public <T, U extends Notifiable> Dep<U> construct(final Function<T, U> constructor, final Dep<T> dep)
	{
		final Dep<U> provider = ConstructorAdaptor.from(constructor, dep);

		eventFabric.connect(provider, ImmutableList.of(dep));

		return provider;
	}
}
