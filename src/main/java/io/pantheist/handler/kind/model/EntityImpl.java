package io.pantheist.handler.kind.model;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.GenericPropertyValue;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.java.model.JavaFileId;

final class EntityImpl implements Entity
{
	private final String entityId;
	private final String kindId;
	private final String jsonSchemaId;
	private final JavaFileId javaFileId;
	private final Map<String, GenericPropertyValue> propertyValues;
	private final boolean canDifferentiate;

	@Inject
	private EntityImpl(
			@Assisted("entityId") final String entityId,
			@Assisted("kindId") final String kindId,
			@Nullable @Assisted("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted final JavaFileId javaFileId,
			@Assisted final Map<String, GenericPropertyValue> propertyValues,
			@Assisted("canDifferentiate") final boolean canDifferentiate)
	{
		this.entityId = OtherPreconditions.checkNotNullOrEmpty(entityId);
		this.kindId = OtherPreconditions.checkNotNullOrEmpty(kindId);
		this.jsonSchemaId = jsonSchemaId;
		this.javaFileId = javaFileId;
		this.propertyValues = ImmutableMap.copyOf(propertyValues);
		this.canDifferentiate = canDifferentiate;
	}

	@Override
	public String entityId()
	{
		return entityId;
	}

	@Override
	public String kindId()
	{
		return kindId;
	}

	@Override
	public String jsonSchemaId()
	{
		return jsonSchemaId;
	}

	@Override
	public JavaFileId javaFileId()
	{
		return javaFileId;
	}

	@Override
	public Map<String, GenericPropertyValue> propertyValues()
	{
		return propertyValues;
	}

	@Override
	public boolean canDifferentiate()
	{
		return canDifferentiate;
	}
}
