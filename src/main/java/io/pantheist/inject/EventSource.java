package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

final class EventSource implements EventOrigin, Dep<EventOrigin>
{
	private final EventFabric eventFabric;

	private EventSource(final EventFabric eventFabric)
	{
		this.eventFabric = checkNotNull(eventFabric);
	}

	static Dep<EventOrigin> in(final EventFabric eventFabric)
	{
		return new EventSource(eventFabric);
	}

	@Override
	public EventOrigin get()
	{
		return this;
	}

	@Override
	public EventType signal(final boolean major)
	{
		return EventType.MINOR;
	}

	@Override
	public void fire()
	{
		eventFabric.signal(this, false);
	}

	@Override
	public String toString()
	{
		return "EventSource";
	}
}
