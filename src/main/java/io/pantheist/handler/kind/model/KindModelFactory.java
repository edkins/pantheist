package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.Presentation;
import io.pantheist.handler.java.model.JavaFileId;

public interface KindModelFactory
{
	Kind kind(
			@Nullable @Assisted("kindId") String kindId,
			KindLevel level,
			@Assisted("discoverable") Boolean discoverable,
			@Nullable JavaClause java,
			@Assisted("partOfSystem") boolean partOfSystem,
			@Assisted("subKindOf") List<String> subKindOf,
			@Assisted("instancePresentation") Presentation instancePresentation);

	Entity entity(
			@Assisted("entityId") String entityId,
			@Assisted("discovered") boolean discovered,
			@Nullable @Assisted("kindId") String kindId,
			@Nullable @Assisted("jsonSchemaId") String jsonSchemaId,
			@Nullable JavaFileId javaFileId);
}
