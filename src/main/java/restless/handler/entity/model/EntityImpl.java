package restless.handler.entity.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class EntityImpl implements Entity
{
	private boolean discovered;
	private final String kindId;
	private final String jsonSchemaId;
	private final String javaPkg;
	private final String javaFile;

	@Inject
	private EntityImpl(
			@Assisted("discovered") @JsonProperty("discovered") final boolean discovered,
			@Nullable @Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Nullable @Assisted("jsonSchemaId") @JsonProperty("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted("javaPkg") @JsonProperty("javaPkg") final String javaPkg,
			@Nullable @Assisted("javaFile") @JsonProperty("javaFile") final String javaFile)
	{
		this.kindId = kindId;
		this.jsonSchemaId = jsonSchemaId;
		this.javaPkg = javaPkg;
		this.javaFile = javaFile;
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
	public String javaPkg()
	{
		return javaPkg;
	}

	@Override
	public String javaFile()
	{
		return javaFile;
	}

	@Override
	public boolean discovered()
	{
		return discovered;
	}

}
