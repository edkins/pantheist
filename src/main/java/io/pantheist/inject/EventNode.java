package io.pantheist.inject;

public interface EventNode
{
	/**
	 * Receives notification of an event, and returns MAJOR or MINOR if it wants
	 * that event to be propagated further downstream.
	 * @param major TODO
	 */
	EventType signal(boolean major);
}
