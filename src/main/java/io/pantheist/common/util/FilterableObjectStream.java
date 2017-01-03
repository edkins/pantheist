package io.pantheist.common.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This is very much like an AntiIterator of ObjectNodes.
 *
 * But the idea is that the filter operations are backed by sql WHERE clauses,
 * so we don't have to read back all of the objects and then discard most of them.
 *
 * After you call one of these methods, you shouldn't use the original object
 * again (although you can chain them). This allows implementations to be mutable.
 */
public interface FilterableObjectStream
{
	/**
	 * Restrict the result objects to include only the given fields names.
	 *
	 * @throws IllegalStateException if you've already called setField
	 * since it's hard to know whether the function would have used one of the fields
	 * you're deleting
	 */
	FilterableObjectStream fields(Collection<String> fieldsNames);

	/**
	 * Restrict results to those where the given field has the given value.
	 *
	 * @throws IllegalStateException if field is one of the ones specified with setField.
	 */
	FilterableObjectStream whereEqual(String field, JsonNode value);

	/**
	 * Applies the given function to each object in the stream, storing the result
	 * in the given field of the object.
	 *
	 * This allows you to annotate objects with extra information, but still have things
	 * further down the chain add additional "whereEqual" conditions, which will still
	 * end up in the SQL.
	 *
	 * It's ok to overwrite a field you've already written here.
	 *
	 * @throws IllegalStateException if fieldName is already present in our list of fields.
	 */
	FilterableObjectStream setField(String fieldName, Function<ObjectNode, JsonNode> fn);

	/**
	 * Apply a filtering step which will be performed after the SQL
	 *
	 * This is almost equivalent to calling antiIt().filter but it does allow further
	 * whereEqual to be performed afterwards.
	 */
	FilterableObjectStream postFilter(Predicate<ObjectNode> predicate);

	/**
	 * Any filtering done after this point won't be backed by SQL.
	 * It'll just be reading things and discarding them.
	 *
	 * Also, whoever's consuming these ObjectNodes is allowed to change them.
	 * A fresh one will be created each time.
	 */
	AntiIterator<ObjectNode> antiIt();
}
