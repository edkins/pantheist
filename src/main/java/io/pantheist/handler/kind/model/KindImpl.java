package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.KindPresentation;

final class KindImpl implements Kind
{
	private static final String PARENT_KIND = "parentKind";
	private final String kindId;
	private final boolean partOfSystem;
	private final KindSchema schema;
	private final KindPresentation presentation;
	private final CreateAction createAction;
	private final DeleteAction deleteAction;

	@Inject
	private KindImpl(
			@Nullable @Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Assisted("partOfSystem") @JsonProperty("partOfSystem") final boolean partOfSystem,
			@Nullable @Assisted @JsonProperty("presentation") final KindPresentation presentation,
			@Assisted @JsonProperty("schema") final KindSchema schema,
			@Nullable @Assisted @JsonProperty("createAction") final CreateAction createAction,
			@Nullable @Assisted @JsonProperty("deleteAction") final DeleteAction deleteAction)
	{
		this.kindId = kindId;
		this.partOfSystem = partOfSystem;
		this.schema = checkNotNull(schema);
		this.presentation = presentation;
		this.createAction = createAction;
		this.deleteAction = deleteAction;
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
}
