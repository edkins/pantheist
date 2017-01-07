package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.KindPresentation;
import io.pantheist.common.util.OtherPreconditions;

final class KindImpl implements Kind
{
	private static final String PARENT_KIND = "parentKind";
	private String kindId;
	private final boolean partOfSystem;
	private final KindSchema schema;
	private final KindPresentation presentation;
	private final CreateAction createAction;
	private final DeleteAction deleteAction;
	private final boolean listable;
	private final JsonNode jsonSchema;
	private final List<Affordance> affordances;
	private final String mimeType;

	private KindImpl(
			@Nullable @JsonProperty("kindId") final String kindId,
			@JsonProperty("partOfSystem") final boolean partOfSystem,
			@Nullable @JsonProperty("presentation") final KindPresentation presentation,
			@JsonProperty("schema") final KindSchema schema,
			@Nullable @JsonProperty("jsonSchema") final JsonNode jsonSchema,
			@Nullable @JsonProperty("mimeType") final String mimeType,
			@Nullable @JsonProperty("createAction") final CreateAction createAction,
			@Nullable @JsonProperty("deleteAction") final DeleteAction deleteAction,
			@JsonProperty("listable") final boolean listable,
			@Nullable @JsonProperty("affordances") final List<Affordance> affordances)
	{
		this.kindId = kindId;
		this.partOfSystem = partOfSystem;
		this.schema = checkNotNull(schema);
		this.jsonSchema = jsonSchema;
		this.mimeType = mimeType;
		this.presentation = presentation;
		this.createAction = createAction;
		this.deleteAction = deleteAction;
		this.listable = listable;
		this.affordances = affordances;
	}

	@Override
	public String kindId()
	{
		return kindId;
	}

	@Override
	public boolean partOfSystem()
	{
		return partOfSystem;
	}

	@Override
	public KindPresentation presentation()
	{
		return presentation;
	}

	@Override
	public KindSchema schema()
	{
		return schema;
	}

	@Override
	public String toString()
	{
		return "[kind " + kindId + "]";
	}

	@Override
	public Optional<String> parent()
	{
		if (schema.identification() != null
				&& schema.identification().has(PARENT_KIND)
				&& schema.identification().get(PARENT_KIND).isTextual())
		{
			return Optional.of(schema.identification().get(PARENT_KIND).textValue());
		}
		else
		{
			return Optional.empty();
		}
	}

	@Override
	public boolean hasParent(final String parentId)
	{
		return parent().equals(Optional.of(parentId));
	}

	@Override
	public boolean isBuiltinKind()
	{
		if (!partOfSystem)
		{
			return false;
		}
		if (schema.identification() != null && schema.identification().has(PARENT_KIND))
		{
			// This would indicate some kind of mistake, as partOfSystem shouldn't be
			// combined with a parent kind.
			return false;
		}
		return true;
	}

	@Override
	public boolean shouldRegisterInSql()
	{
		if (!isBuiltinKind())
		{
			return false;
		}
		if (schema.properties() == null || schema.properties().isEmpty())
		{
			return false;
		}
		return true;
	}

	@Override
	public CreateAction createAction()
	{
		return createAction;
	}

	@Override
	public DeleteAction deleteAction()
	{
		return deleteAction;
	}

	@Override
	public boolean listable()
	{
		return listable;
	}

	@Override
	public JsonNode jsonSchema()
	{
		return jsonSchema;
	}

	@Override
	public void setKindId(final String kindId)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		if (this.kindId != null)
		{
			throw new IllegalStateException("setKindId can only be used to set null kindId");
		}
		this.kindId = kindId;
	}

	@Override
	public List<Affordance> affordances()
	{
		return affordances;
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}
}
