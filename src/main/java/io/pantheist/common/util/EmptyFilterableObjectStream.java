package io.pantheist.common.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Whatever you do, you'll carry on getting nothing.
 */
public final class EmptyFilterableObjectStream implements FilterableObjectStream
{
	private static final FilterableObjectStream EMPTY = new EmptyFilterableObjectStream();

	private EmptyFilterableObjectStream()
	{
	}

	public static FilterableObjectStream empty()
	{
		return EMPTY;
	}

	@Override
	public FilterableObjectStream fields(final Collection<String> fieldsNames)
	{
		return this;
	}

	@Override
	public FilterableObjectStream whereEqual(final String field, final JsonNode value)
	{
		return this;
	}

	@Override
	public FilterableObjectStream setField(final String fieldName, final Function<ObjectNode, JsonNode> fn)
	{
		return this;
	}

	@Override
	public FilterableObjectStream postFilter(final Predicate<ObjectNode> predicate)
	{
		return this;
	}

	@Override
	public AntiIterator<ObjectNode> antiIt()
	{
		return AntiIt.empty();
	}
}
