package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface KindModelFactory
{
	Kind kind(
			@Nullable @Assisted("kindId") String kindId,
			KindLevel level,
			@Assisted("discoverable") Boolean discoverable,
			@Nullable JavaClause java,
			@Assisted("partOfSystem") boolean partOfSystem,
			@Assisted("subKindOf") List<String> subKindOf);
}
