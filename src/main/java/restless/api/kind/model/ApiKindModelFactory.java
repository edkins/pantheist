package restless.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.ListClassifierItem;
import restless.handler.kind.model.JavaClause;
import restless.handler.kind.model.KindLevel;

public interface ApiKindModelFactory
{
	ApiKind kind(
			@Nullable List<ListClassifierItem> childResources,
			@Nullable @Assisted("kindId") String kindId,
			KindLevel level,
			@Assisted("discoverable") Boolean discoverable,
			@Nullable JavaClause java,
			@Assisted("partOfSystem") boolean partOfSystem);

	ListKindResponse listKindResponse(List<ListKindItem> childResources);

	ListKindItem listKindItem(@Assisted("url") String url);
}
