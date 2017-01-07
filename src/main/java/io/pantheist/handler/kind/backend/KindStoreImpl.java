package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindProperty;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.handler.sql.model.SqlModelFactory;
import io.pantheist.handler.sql.model.SqlProperty;

final class KindStoreImpl implements KindStore
{
	private final SqlService sqlService;
	private final SqlModelFactory sqlFactory;
	private final CommonSharedModelFactory sharedFactory;
	private final FileKindHandler fileKindHandler;

	@Inject
	private KindStoreImpl(
			final SqlService sqlService,
			final SqlModelFactory sqlFactory,
			final CommonSharedModelFactory sharedFactory,
			final FileKindHandler fileKindHandler)
	{
		this.sqlService = checkNotNull(sqlService);
		this.sqlFactory = checkNotNull(sqlFactory);
		this.sharedFactory = checkNotNull(sharedFactory);
		this.fileKindHandler = checkNotNull(fileKindHandler);
	}

	private SqlProperty toSqlProperty(final Entry<String, KindProperty> e)
	{
		return sqlFactory.property(
				e.getKey(),
				e.getValue().typeInfo(sharedFactory),
				e.getValue().isIdentifier());
	}

	@Override
	public void registerKindsInSql()
	{
		fileKindHandler.listAllKinds().forEach(k -> {
			if (k.shouldRegisterInSql())
			{
				final List<SqlProperty> properties = k.schema()
						.properties()
						.entrySet()
						.stream()
						.map(this::toSqlProperty)
						.collect(Collectors.toList());
				sqlService.createTable(k.kindId(), properties);
			}
		});
	}

	@Override
	public AntiIterator<SqlProperty> listSqlPropertiesOfKind(final String kindId)
	{
		final Optional<Kind> kind = fileKindHandler.getKind(kindId);
		if (!kind.isPresent() || !kind.get().shouldRegisterInSql())
		{
			return AntiIt.empty();
		}
		return AntiIt.from(kind.get().schema()
				.properties()
				.entrySet())
				.map(this::toSqlProperty);
	}
}
