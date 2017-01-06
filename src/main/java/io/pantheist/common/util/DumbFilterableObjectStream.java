package io.pantheist.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

final class DumbFilterableObjectStream implements FilterableObjectStream
{
	private final AntiIterator<ObjectNode> antiIt;

	private DumbFilterableObjectStream(final AntiIterator<ObjectNode> antiIt)
	{
		this.antiIt = checkNotNull(antiIt);
	}

	static FilterableObjectStream of(final AntiIterator<ObjectNode> antiIt)
	{
		return new DumbFilterableObjectStream(antiIt);
	}

	@Override
	public FilterableObjectStream fields(final Collection<String> fieldNames)
	{
		return of(antiIt.map(obj -> {
			final ImmutableList<String> fields = ImmutableList.copyOf(obj.fieldNames());
			for (final String field : fields)
			{
				if (!fieldNames.contains(field))
				{
					obj.remove(field);
				}
			}
			return obj;
		}));
	}

	@Override
	public FilterableObjectStream whereEqual(final String field, final JsonNode value)
	{
		return of(antiIt.filter(obj -> obj.get(field).equals(value)));
	}

	@Override
	public FilterableObjectStream setField(final String fieldName, final Function<ObjectNode, JsonNode> fn)
	{
		return of(antiIt.map(obj -> {
			obj.set(fieldName, fn.apply(obj));
			return obj;
		}));
	}

	@Override
	public FilterableObjectStream postFilter(final Predicate<ObjectNode> predicate)
	{
		return of(antiIt.filter(predicate));
	}

	@Override
	public AntiIterator<ObjectNode> antiIt()
	{
		return antiIt;
	}

}
