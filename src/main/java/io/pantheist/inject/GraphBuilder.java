package io.pantheist.inject;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface GraphBuilder
{
	/**
	 * Always provides the given object, and never signals a change.
	 */
	<T> Dep<T> singleton(T object);

	/**
	 * Provides the initial value, but the interface also gives a way to change it,
	 * triggering a new change event.
	 *
	 * This is useful for testing.
	 */
	<T> MutableProvider<T> mutable(T initialValue);

	/**
	 * Apply the given constructor function to the dependency.
	 *
	 * When the dependency signals a major change, the function will be immediately reapplied
	 * in order to create a new object.
	 *
	 * When the dependency signals a minor change, the object will not be recreated but
	 * it will be notified. If it returns true then the event will continue to be propagated
	 * as a minor change.
	 *
	 * The notification will *not* be called in the case of a major change. It's assumed that
	 * a brand new object will be created in this case so it's not necessary.
	 */
	<T, U extends Notifiable> Dep<U> construct(Function<T, U> constructor, Dep<T> dep);

	/**
	 * Apply the given function to the dependency.
	 *
	 * When the dependency signals a major change, the function will be immediately reapplied and
	 * a new value computed.
	 *
	 * When the dependency signals a minor change, it will be propagated as a minor change
	 * but the function will not be reapplied.
	 *
	 * This is like {@link #construct(Function, Dep)} but deals with objects that are oblivious
	 * to events.
	 */
	<T, U> Dep<U> oblivious(Function<T, U> fn, Dep<T> dep);

	/**
	 * Apply the given function to two dependencies.
	 *
	 * When either (or both) dependencies signals a change, the function will be immediately reapplied and
	 * a new value computed.
	 *
	 * If both dependencies change as a result of a single original event, the function will only be
	 * reapplied once.
	 *
	 * When the dependency signals a minor change, it will be propagated as a minor change
	 * but the function will not be reapplied.
	 */
	<T, U, R> Dep<R> oblivious2(BiFunction<T, U, R> fn, Dep<T> dep1,
			Dep<U> dep2);

	/**
	 * Provides the same values that its dependency provides, but change events will be absorbed
	 * (i.e. not propagated) if the new value is identical to the old one.
	 *
	 * (It has to be identical, i.e compare true with ==, not merely equal).
	 */
	<T> Dep<T> absorb(Dep<T> dep);

	/**
	 * Provides the same values that its dependency provides, but change events will be absorbed
	 * (i.e. not propagated) if the new value is equal to the old one.
	 */
	<T> Dep<T> absorbEqual(Dep<T> dep);

	/**
	 * Used for injecting dependencies indirectly, via a supplier.
	 *
	 * Change events will be converted to minor change events. This means that if the consumer
	 * is created with {@link #install(Function, Dep)}, the resulting object will receive a
	 * notification when the original dep changes, but will not be rebuilt.
	 *
	 * If dep itself signals a minor change event, this will still be propagated as a
	 * minor change event.
	 */
	<T> Dep<Supplier<T>> supplier(Dep<T> dep);

	/**
	 * Objects will be created given the supplied constructor.
	 *
	 * This is eager, so one object will be created initially, and more will be created whenever the
	 * dependency signals a major change.
	 *
	 * The object will be notified of any minor changes. If its {@link NotifiableSupplier#signal()} method
	 * returns true, the event will be upgraded to a major change and propagated.
	 *
	 * If it returns false then the event will not be propagated.
	 *
	 * Since this turns minor events into major, and {@link #supplier(Dep)} turns major events
	 * into minor, they can be chained together to keep the minor events minor, with the
	 * effect that whoever's consuming this object will instead see it as a Supplier.
	 */
	<T, U> Dep<U> install(Function<T, NotifiableSupplier<U>> constructor, Dep<T> dep);

	/**
	 * When injected using install, this will appear as an EventOrigin object.
	 *
	 */
	Dep<EventOrigin> eventSource();
}
