package io.pantheist.inject;

import java.util.List;

interface EventFabric
{
	/**
	 * origin itself will not be notified, but anything downstream of it will.
	 */
	void signal(EventNode origin, boolean major);

	void connect(EventNode sink, List<EventNode> sources);
}
