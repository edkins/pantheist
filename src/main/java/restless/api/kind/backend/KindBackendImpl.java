package restless.api.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.inject.Inject;

import restless.api.kind.model.ApiComponent;
import restless.api.kind.model.ApiEntity;
import restless.api.kind.model.ApiKindModelFactory;
import restless.api.kind.model.ListComponentItem;
import restless.api.kind.model.ListComponentResponse;
import restless.common.util.FailureReason;
import restless.common.util.OtherCollectors;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.entity.backend.EntityStore;
import restless.handler.entity.model.Entity;
import restless.handler.entity.model.EntityModelFactory;
import restless.handler.java.backend.JavaStore;
import restless.handler.java.model.JavaComponent;
import restless.handler.kind.backend.KindStore;
import restless.handler.kind.model.Kind;
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

	@Inject
	private KindBackendImpl(
			final EntityStore entityStore,
			final UrlTranslation urlTranslation,
			final EntityModelFactory entityFactory,
			final KindStore kindStore,
			final JsonSchemaStore schemaStore,
			final JavaStore javaStore,
			final ApiKindModelFactory modelFactory)
	{
		this.entityStore = checkNotNull(entityStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.entityFactory = checkNotNull(entityFactory);
		this.kindStore = checkNotNull(kindStore);
		this.schemaStore = checkNotNull(schemaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.javaStore = checkNotNull(javaStore);
	}

	@Override
	public Possible<Void> putEntity(final String entityId, final ApiEntity entity)
	{
		// We don't validate against kind here. It's ok to put invalid things.
		return entityStore.putEntity(entityId, fromApiEntity(entity));
	}

	@Override
	public Possible<ApiEntity> getEntity(final String entityId)
	{
		return entityStore.getEntity(entityId).map(this::toApiEntity);
	}

	@Override
	public Possible<ApiComponent> getComponent(final String entityId, final String componentId)
	{
		return entityStore.getEntity(entityId).posMap(e -> {
			SchemaComponent jsonSchema = null;
			JavaComponent java = null;
			if (e.jsonSchemaId() != null)
			{
				jsonSchema = schemaStore.getJsonSchemaComponent(e.jsonSchemaId(), componentId).orElse(null);
			}
			if (e.javaFile() != null)
			{
				checkNotNull(e.javaPkg());
				java = javaStore.getJavaComponent(e.javaPkg(), e.javaFile(), componentId).orElse(null);
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
		return entityStore.getEntity(entityId).map(e -> {
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

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind)
	{
		return kindStore.putKind(kindId, kind);
	}

	private boolean validateEntityAgainstKind(final Entity entity)
	{
		if (entity.kindId() == null)
		{
			// If kind is missing then anything is valid.
			return true;
		}
		final Kind kind = getKind(entity.kindId()).get();
		if (kind.java() != null)
		{
			if (kind.java().required() && (entity.javaPkg() == null || entity.javaFile() == null))
			{
				// Java is required but not present in entity.
				return false;
			}
			if (!javaStore.validateKind(entity.javaPkg(), entity.javaFile(), kind.java()))
			{
				// Java store says the details are invalid.
				return false;
			}
		}

		// Otherwise assume ok.
		return true;
	}

	private Entity fromApiEntity(final ApiEntity entity)
	{
		return entityFactory.entity(
				nullable(urlTranslation::kindFromUrl, entity.kindUrl()),
				nullable(urlTranslation::jsonSchemaFromUrl, entity.jsonSchemaUrl()),
				nullable(urlTranslation::javaPkgFromUrl, entity.javaUrl()),
				nullable(urlTranslation::javaFileFromUrl, entity.javaUrl()));
	}

	private ApiEntity toApiEntity(final Entity entity)
	{
		final boolean valid = validateEntityAgainstKind(entity);
		return modelFactory.entity(
				nullable(urlTranslation::kindToUrl, entity.kindId()),
				nullable(urlTranslation::jsonSchemaToUrl, entity.jsonSchemaId()),
				nullable2(urlTranslation::javaToUrl, entity.javaPkg(), entity.javaFile()),
				valid);
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

	@Nullable
	private <T, U, V> V nullable2(final BiFunction<T, U, V> fn, @Nullable final T x, @Nullable final U y)
	{
		if (x == null && y == null)
		{
			return null;
		}
		else if (x != null && y != null)
		{
			return fn.apply(x, y);
		}
		else
		{
			throw new NullPointerException("Either both must be null, or neither");
		}
	}

}
