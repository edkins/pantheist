package restless.api.entity.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import restless.api.entity.model.ApiComponent;
import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ApiEntityModelFactory;
import restless.api.entity.model.ListComponentItem;
import restless.api.entity.model.ListComponentResponse;
import restless.api.entity.model.ListEntityItem;
import restless.api.entity.model.ListEntityResponse;
import restless.api.kind.model.ApiKindModelFactory;
import restless.common.api.url.UrlTranslation;
import restless.common.util.AntiIterator;
import restless.common.util.FailureReason;
import restless.common.util.OtherCollectors;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.Entity;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaComponent;
import restless.handler.java.model.JavaFileId;
import restless.handler.kind.backend.KindValidation;
import restless.handler.schema.backend.JsonSchemaStore;
import restless.handler.schema.model.SchemaComponent;

final class EntityBackendImpl implements EntityBackend
{
	private final EntityStore entityStore;
	private final UrlTranslation urlTranslation;
	private final EntityModelFactory entityFactory;
	private final JsonSchemaStore schemaStore;
	private final JavaStore javaStore;
	private final KindValidation kindValidation;
	private final ApiEntityModelFactory modelFactory;

	@Inject
	private EntityBackendImpl(
			final EntityStore entityStore,
			final UrlTranslation urlTranslation,
			final EntityModelFactory entityFactory,
			final JsonSchemaStore schemaStore,
			final JavaStore javaStore,
			final ApiKindModelFactory modelFactory,
			final KindValidation kindValidation,
			final ApiEntityModelFactory apiEntityFactory)
	{
		this.entityStore = checkNotNull(entityStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.entityFactory = checkNotNull(entityFactory);
		this.schemaStore = checkNotNull(schemaStore);
		this.javaStore = checkNotNull(javaStore);
		this.kindValidation = checkNotNull(kindValidation);
		this.modelFactory = checkNotNull(apiEntityFactory);
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
		final boolean valid = kindValidation.validateEntityAgainstStoredKind(entity);
		return modelFactory.entity(
				entity.discovered(),
				nullable(urlTranslation::kindToUrl, entity.kindId()),
				nullable(urlTranslation::jsonSchemaToUrl, entity.jsonSchemaId()),
				nullable(urlTranslation::javaToUrl, entity.javaFileId()),
				valid,
				urlTranslation.listEntityClassifiers(entity.entityId()));
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

	@Override
	public Possible<ApiComponent> getComponent(final String entityId, final String componentId)
	{
		return findEntity(entityId).<ApiComponent>posMap(e -> {
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
		return findEntity(entityId).<ListComponentResponse>map(e -> {
			final Map<String, ListComponentItem> result = new HashMap<>();

			if (e.jsonSchemaId() != null)
			{
				schemaStore.listComponents(e.jsonSchemaId()).forEach(c -> {
					result.put(c.componentId(), modelFactory.listComponentItem(
							urlTranslation.componentToUrl(entityId, c.componentId()),
							c.componentId()));
				});

			}
			return result.values().stream().collect(OtherCollectors.wrapped(modelFactory::listComponentResponse));
		});
	}

	private ListEntityItem toListEntityItem(final Entity entity)
	{
		return modelFactory.listEntityItem(
				urlTranslation.entityToUrl(entity.entityId()),
				entity.entityId(),
				entity.discovered());
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

	private AntiIterator<Entity> discoverJavaEntities()
	{
		return javaStore.allJavaFiles()
				.map(jf -> entityFactory.entity(jf.file(), true, null, null, jf))
				.optMap(kindValidation::discoverKind);
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
				final Optional<Entity> entityWithKind = kindValidation.discoverKind(entityWithoutKind);
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

}
