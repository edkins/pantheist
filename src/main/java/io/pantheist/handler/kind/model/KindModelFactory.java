package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.KindPresentation;

public interface KindModelFactory
{
	Kind kind(
			@Assisted("kindId") String kindId,
			KindSchema schema,
			@Assisted("partOfSystem") boolean partOfSystem,
			@Nullable KindPresentation instancePresentation,
			@Nullable CreateAction createAction,
			@Nullable DeleteAction deleteAction);
}
