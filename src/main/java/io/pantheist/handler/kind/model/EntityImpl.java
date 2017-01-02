package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.java.model.JavaFileId;

final class EntityImpl implements Entity
{
	private final String entityId;
	private final String kindId;
	private final String jsonSchemaId;
	private final JavaFileId javaFileId;

	@Inject
	private EntityImpl(
			@Assisted("entityId") @JsonProperty("entityId") final String entityId,
			@Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Nullable @Assisted("jsonSchemaId") @JsonProperty("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted @JsonProperty("javaFileId") final JavaFileId javaFileId)
	{
		this.entityId = OtherPreconditions.checkNotNullOrEmpty(entityId);
		this.kindId = OtherPreconditions.checkNotNullOrEmpty(kindId);
		this.jsonSchemaId = jsonSchemaId;
		this.javaFileId = javaFileId;
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
}
