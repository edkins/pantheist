package io.pantheist.handler.kind.model;

import java.util.Map;

import javax.annotation.Nullable;

import io.pantheist.common.shared.model.GenericPropertyValue;
import io.pantheist.handler.java.model.JavaFileId;

public interface Entity
{
	String entityId();

	String kindId();

	@Nullable
	String jsonSchemaId();

	@Nullable
	JavaFileId javaFileId();

	Map<String, GenericPropertyValue> propertyValues();

	/**
	 * Used internally to signify that the entity is a candidate for further
	 * differentiation into a subkind.
	 *
	 * If false it usually means the entity is not fully valid.
	 *
	 * If true, all properties specified in the kind must have corresponding values in
	 * this entity.
	 */
	boolean canDifferentiate();
}
