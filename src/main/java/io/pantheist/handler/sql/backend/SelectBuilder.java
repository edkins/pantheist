package io.pantheist.handler.sql.backend;

import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.util.AntiIterator;

/**
 * Mutable builder for SELECT statements.
 */
public interface SelectBuilder
{
	/**
	 * Restrict the result objects to include only the given column names.
	 *
	 * (By default it will return everything in your list of sql properties)
	 *
	 * @return this
	 */
	SelectBuilder columns(Collection<String> columnNames);

	/**
	 * Restrict results to those where the given column has the given value.
	 *
	 * @return this
	 */
	SelectBuilder whereEqual(String column, JsonNode value);

	AntiIterator<ObjectNode> execute();
}
