package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.Presentation;
import io.pantheist.handler.java.model.JavaFileId;

public interface KindModelFactory
{
	Kind kind(
			@Assisted("kindId") String kindId,
			KindSchema schema,
			@Assisted("partOfSystem") boolean partOfSystem,
			@Nullable @Assisted("instancePresentation") Presentation instancePresentation);

	KindSchema schema(
			@Nullable JavaClause java,
			@Assisted("subKindOf") List<String> subKindOf,
			@Assisted("instancePresentation") Presentation instancePresentation);

	Entity entity(
			@Assisted("entityId") String entityId,
			@Assisted("discovered") boolean discovered,
			@Nullable @Assisted("kindId") String kindId,
			@Nullable @Assisted("jsonSchemaId") String jsonSchemaId,
			@Nullable JavaFileId javaFileId);
}
