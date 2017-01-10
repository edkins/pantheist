package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

final class EventFabricImpl implements EventFabric
{
	// State
	private final List<Connection> connections;

	EventFabricImpl()
	{
		this.connections = new ArrayList<>();
	}

	@Override
	public void signal(final EventNode origin, final boolean major)
	{
		final List<Connection> cs = ImmutableList.copyOf(connections);
		final Map<EventNode, EventType> active = new HashMap<>();
		active.put(origin, major ? EventType.MAJOR : EventType.MINOR);
		for (final Connection c : cs)
		{
			final EventType incomingEvent = c.sources.stream()
					.filter(active::containsKey)
					.map(active::get)
					.reduce(EventType::max)
					.orElse(EventType.NONE);
			if (incomingEvent != EventType.NONE)
			{
				final EventType outgoingEvent = c.sink.signal(incomingEvent == EventType.MAJOR);
				if (outgoingEvent != EventType.NONE)
				{
					active.put(c.sink, EventType.max(outgoingEvent, active.get(c.sink)));
				}
			}
		}
	}

	@Override
	public void connect(final EventNode sink, final List<EventNode> sources)
	{
		checkNotNull(sink);
		checkNotNull(sources);
		if (connections.stream().anyMatch(c -> c.sink.equals(sink)))
		{
			throw new IllegalStateException("Already registered event sink: " + sink);
		}
		for (final EventNode source : sources)
		{
			if (!connections.stream().anyMatch(c -> c.sink.equals(source)))
			{
				throw new IllegalStateException("Have not yet registered event source: " + source);
			}
		}
		connections.add(new Connection(sink, sources));
	}

	private static final class Connection
	{
		private final EventNode sink;
		private final List<EventNode> sources;

		private Connection(final EventNode sink, final List<EventNode> sources)
		{
			this.sink = checkNotNull(sink);
			this.sources = ImmutableList.copyOf(sources);
		}

		@Override
		public String toString()
		{
			return sources + " -> " + sink;
		}
	}
}
