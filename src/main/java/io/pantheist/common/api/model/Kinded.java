package io.pantheist.common.api.model;

/**
 * Arbitrary data annotated with a kindUrl.
 */
public interface Kinded<T>
{
	String kindUrl();

	T data();
}
