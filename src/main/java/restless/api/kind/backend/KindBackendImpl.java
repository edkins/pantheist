package restless.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import restless.api.kind.model.ApiComponent;
import restless.api.kind.model.ApiEntity;
import restless.api.kind.model.ApiKindModelFactory;
import restless.api.kind.model.ListComponentItem;
import restless.api.kind.model.ListComponentResponse;
import restless.api.kind.model.ListEntityItem;
import restless.api.kind.model.ListEntityResponse;
import restless.common.util.AntiIterator;
import restless.common.util.FailureReason;
import restless.common.util.OtherCollectors;
import restless.common.util.OtherPreconditions;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.Entity;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaComponent;
import restless.handler.java.model.JavaFileId;
import restless.handler.kind.backend.KindStore;
import restless.handler.kind.model.Kind;
import restless.handler.kind.model.KindModelFactory;
import restless.handler.schema.backend.JsonSchemaStore;
import restless.handler.schema.model.SchemaComponent;
import restless.handler.uri.UrlTranslation;

final class KindBackendImpl implements KindBackend
{
	private final EntityStore entityStore;
	private final UrlTranslation urlTranslation;
	private final EntityModelFactory entityFactory;
	private final KindStore kindStore;
	private final JsonSchemaStore schemaStore;
	private final ApiKindModelFactory modelFactory;
	private final JavaStore javaStore;
	private final KindModelFactory kindFactory;

	@Inject
	private KindBackendImpl(
			final EntityStore entityStore,
			final UrlTranslation urlTranslation,
			final EntityModelFactory entityFactory,
			final KindStore kindStore,
			final JsonSchemaStore schemaStore,
			final JavaStore javaStore,
			final ApiKindModelFactory modelFactory,
			final KindModelFactory kindFactory)
	{
		this.entityStore = checkNotNull(entityStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.entityFactory = checkNotNull(entityFactory);
		this.kindStore = checkNotNull(kindStore);
		this.schemaStore = checkNotNull(schemaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.javaStore = checkNotNull(javaStore);
		this.kindFactory = checkNotNull(kindFactory);
	}

	@Override
	public Possible<Void> putApiEntity(final String entityId, final ApiEntity entity)
	{
		// We don't validate against kind here. It's ok to put invalid things.
		if (entity.discovered())
		{
			// The request was syntactically valid, but we cannot store "discovered" entities.
			return FailureReason.REQUEST_INVALID_OPERATION.happened();
		}
		entityStore.putEntity(entityId, fromApiEntity(entityId, entity));
		return View.noContent();
	}

	@Override
	public Possible<ApiEntity> getApiEntity(final String entityId)
	{
		return findEntity(entityId).map(this::toApiEntity);
	}

	private Optional<Entity> discoverKind(final Entity entity)
	{
		return kindStore.discoverKinds()
				.filter(k -> validateEntityAgainstKind(entity, k))
				.failIfMultiple()
				.map(k -> supplyKind(entity, k));
	}

	private Possible<Entity> findEntity(final String entityId)
	{
		final Optional<Entity> storedEntity = entityStore.getEntity(entityId);
		if (storedEntity.isPresent())
		{
			return View.ok(storedEntity.get());
		}
		else
		{
			final Optional<JavaFileId> javaFile = javaStore.findFileByName(entityId);
			if (javaFile.isPresent())
			{
				final Entity entityWithoutKind = entityFactory.entity(entityId, true, null, null,
						javaFile.orElse(null));
				final Optional<Entity> entityWithKind = discoverKind(entityWithoutKind);
				if (entityWithKind.isPresent())
				{
					return View.ok(entityWithKind.get());
				}
				else
				{
					return FailureReason.DOES_NOT_EXIST.happened();
				}
			}
			else
			{
				return FailureReason.DOES_NOT_EXIST.happened();
			}
		}
	}

	private Entity supplyKind(final Entity entity, final Kind kind)
	{
		if (entity.kindId() != null)
		{
			throw new IllegalArgumentException("entity should not have kind yet");
		}
		return entityFactory.entity(
				entity.entityId(),
				entity.discovered(),
				kind.kindId(),
				entity.jsonSchemaId(),
				entity.javaFileId());
	}

	@Override
	public Possible<ApiComponent> getComponent(final String entityId, final String componentId)
	{
		return findEntity(entityId).posMap(e -> {
			SchemaComponent jsonSchema = null;
			JavaComponent java = null;
			if (e.jsonSchemaId() != null)
			{
				jsonSchema = schemaStore.getJsonSchemaComponent(e.jsonSchemaId(), componentId).orElse(null);
			}
			if (e.javaFileId() != null)
			{
				java = javaStore.getJavaComponent(e.javaFileId(), componentId).orElse(null);
			}

			if (jsonSchema != null || java != null)
			{
				return View.ok(modelFactory.component(jsonSchema, java));
			}
			else
			{
				// None of the handlers know anything about this component.
				return FailureReason.DOES_NOT_EXIST.happened();
			}
		});
	}

	@Override
	public Possible<ListComponentResponse> listComponents(final String entityId)
	{
		return findEntity(entityId).map(e -> {
			final Map<String, ListComponentItem> result = new HashMap<>();

			if (e.jsonSchemaId() != null)
			{
				schemaStore.listComponents(e.jsonSchemaId()).forEach(c -> {
					result.put(c.componentId(), modelFactory.listComponentItem(c.componentId()));
				});
			}
			return result.values().stream().collect(OtherCollectors.wrapped(modelFactory::listComponentResponse));
		});
	}

	@Override
	public Possible<Kind> getKind(final String kindId)
	{
		return kindStore.getKind(kindId);
	}

	private Kind supplyKindId(final String kindId, final Kind kind)
	{
		OtherPreconditions.checkNotNullOrEmpty(kindId);
		return kindFactory.kind(kindId, kind.level(), kind.discoverable(), kind.java());
	}

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind)
	{
		if (kind.kindId() != null && !kind.kindId().equals(kindId))
		{
			return FailureReason.WRONG_LOCATION.happened();
		}
		return kindStore.putKind(kindId, supplyKindId(kindId, kind));
	}

	private boolean validateEntityAgainstKind(final Entity entity, final Kind kind)
	{
		if (kind.java() != null)
		{
			if (kind.java().required() && (entity.javaFileId() == null))
			{
				// Java is required but not present in entity.
				return false;
			}
			if (!javaStore.validateKind(entity.javaFileId(), kind.java()))
			{
				// Java store says the details are invalid.
				return false;
			}
		}

		// Otherwise assume ok.
		return true;
	}

	private Entity fromApiEntity(final String entityId, final ApiEntity entity)
	{
		return entityFactory.entity(
				entityId,
				entity.discovered(),
				nullable(urlTranslation::kindFromUrl, entity.kindUrl()),
				nullable(urlTranslation::jsonSchemaFromUrl, entity.jsonSchemaUrl()),
				nullable(urlTranslation::javaFromUrl, entity.javaUrl()));
	}

	private ApiEntity toApiEntity(final Entity entity)
	{
		final boolean valid = validateEntityAgainstStoredKind(entity);
		return modelFactory.entity(
				entity.discovered(),
				nullable(urlTranslation::kindToUrl, entity.kindId()),
				nullable(urlTranslation::jsonSchemaToUrl, entity.jsonSchemaId()),
				nullable(urlTranslation::javaToUrl, entity.javaFileId()),
				valid);
	}

	private boolean validateEntityAgainstStoredKind(final Entity entity)
	{
		if (entity.kindId() == null)
		{
			// If kind is missing then anything is valid.
			return true;
		}
		final Kind kind = getKind(entity.kindId()).get();
		return validateEntityAgainstKind(entity, kind);
	}

	@Nullable
	private <T, U> U nullable(final Function<T, U> fn, @Nullable final T x)
	{
		if (x == null)
		{
			return null;
		}
		else
		{
			return fn.apply(x);
		}
	}

	private ListEntityItem toListEntityItem(final Entity entity)
	{
		return modelFactory.listEntityItem(entity.entityId(), entity.discovered());
	}

	@Override
	public ListEntityResponse listEntities()
	{
		final ImmutableMap.Builder<String, Entity> builder = ImmutableMap.builder();

		// Conflicts (i.e. things that generate the same entityId) will cause exceptions here.

		entityStore.listEntities().forEach(e -> builder.put(e.entityId(), e));

		discoverJavaEntities().forEach(e -> builder.put(e.entityId(), e));

		return modelFactory.listEntityResponse(
				builder.build()
						.values()
						.stream()
						.map(this::toListEntityItem)
						.collect(Collectors.toList()));
	}

	private AntiIterator<Entity> discoverJavaEntities()
	{
		return javaStore.allJavaFiles()
				.map(jf -> entityFactory.entity(jf.file(), true, null, null, jf))
				.optMap(this::discoverKind);
	}
}
