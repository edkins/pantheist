package io.pantheist.inject;

import javax.annotation.Nullable;

/**
 * MAJOR signifies that one of the dependencies is now supplying a new object.
 * Most Dep implementations will react to this by rebuilding the object that they
 * supply.
 *
 * But {@link SupplierSupplier} will instead downgrade it to a minor event.
 *
 * MINOR signifies that the dependencies are all still supplying the same objects,
 * but some aspect of them has changed. This is for when the objects themselves are
 * suppliers or collections of some kind. The event doesn't really make sense if
 * we're talking about immutable data objects.
 *
 * NONE is used as a return value from {@link EventNode} to signal that the event
 * should not be propagated.
 */
enum EventType
{
	MAJOR,
	MINOR,
	NONE;

	static EventType max(@Nullable final EventType a, @Nullable final EventType b)
	{
		if (a == MAJOR || b == MAJOR)
		{
			return MAJOR;
		}
		if (a == MINOR || b == MINOR)
		{
			return MINOR;
		}
		return NONE;
	}
}
