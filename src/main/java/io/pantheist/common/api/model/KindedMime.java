package io.pantheist.common.api.model;

/**
 * Represents data of a particular kind and mime type (that we just don't happen to know at compile time)
 */
public interface KindedMime
{
	String kindUrl();

	String mimeType();

	String text();
}
