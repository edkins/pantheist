package restless.handler.entity.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.handler.java.model.JavaFileId;

final class EntityImpl implements Entity
{
	private final boolean discovered;
	private final String kindId;
	private final String jsonSchemaId;
	private final JavaFileId javaFileId;

	@Inject
	private EntityImpl(
			@Assisted("discovered") @JsonProperty("discovered") final boolean discovered,
			@Nullable @Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Nullable @Assisted("jsonSchemaId") @JsonProperty("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted @JsonProperty("javaFileId") final JavaFileId javaFileId)
	{
		this.discovered = discovered;
		this.kindId = kindId;
		this.jsonSchemaId = jsonSchemaId;
		this.javaFileId = javaFileId;
	}

	@Override
	public boolean discovered()
	{
		return discovered;
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
