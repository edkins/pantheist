package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;

final class JsonPathImpl implements JsonPath
{
	private final String path;

	@JsonCreator
	private JsonPathImpl(final String path)
	{
		this.path = checkNotNull(path);
	}

	@Override
	public String toString()
	{
		return path;
	}
}
