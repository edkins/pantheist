package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.java.model.JavaFileId;

final class EntityImpl implements Entity
{
	private final String entityId;
	private final String kindId;
	private final String jsonSchemaId;
	private final JavaFileId javaFileId;
	private final ObjectNode propertyValues;
	private final boolean canDifferentiate;

	@Inject
	private EntityImpl(
			@Assisted("entityId") final String entityId,
			@Assisted("kindId") final String kindId,
			@Nullable @Assisted("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted final JavaFileId javaFileId,
			@Assisted final ObjectNode propertyValues,
			@Assisted("canDifferentiate") final boolean canDifferentiate)
	{
		this.entityId = OtherPreconditions.checkNotNullOrEmpty(entityId);
		this.kindId = OtherPreconditions.checkNotNullOrEmpty(kindId);
		this.jsonSchemaId = jsonSchemaId;
		this.javaFileId = javaFileId;
		this.propertyValues = checkNotNull(propertyValues);
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
	public ObjectNode propertyValues()
	{
		return propertyValues;
	}

	@Override
	public boolean canDifferentiate()
	{
		return canDifferentiate;
	}
}
