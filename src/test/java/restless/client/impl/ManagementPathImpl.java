package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import restless.api.management.model.ApiComponent;
import restless.api.management.model.ApiEntity;
import restless.api.management.model.ListComponentItem;
import restless.api.management.model.ListComponentResponse;
import restless.api.management.model.ListConfigItem;
import restless.api.management.model.ListConfigResponse;
import restless.client.api.ManagementData;
import restless.client.api.ManagementDataSchema;
import restless.client.api.ManagementPathEntity;
import restless.client.api.ManagementPathJavaPackage;
import restless.client.api.ManagementPathKind;
import restless.client.api.ManagementPathLocation;
import restless.client.api.ManagementPathRoot;
import restless.client.api.ManagementPathServer;
import restless.client.api.ResponseType;
import restless.handler.kind.model.Kind;

final class ManagementPathImpl implements
		ManagementPathServer,
		ManagementPathLocation,
		ManagementPathRoot,
		ManagementPathJavaPackage,
		ManagementPathEntity,
		ManagementPathKind
{
	// Path segments
	private static final String JAVA_PKG = "java-pkg";
	private static final String FILE = "file";
	private static final String DATA = "data";
	private static final String LOCATION = "location";
	private static final String SERVER = "server";
	private static final String JSON_SCHEMA = "json-schema";
	private static final String ENTITY = "entity";
	private static final String COMPONENT = "component";

	// Content types
	private static final String APPLICATION_JSON = "application/json";

	// Collaborators
	private final TargetWrapper target;

	ManagementPathImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public ManagementPathServer server(final int port)
	{
		return new ManagementPathImpl(target.withSegment(SERVER).withSegment(String.valueOf(port)));
	}

	@Override
	public ManagementPathLocation location(final String path)
	{
		return new ManagementPathImpl(target.withSegment(LOCATION).withEscapedSegment(path));
	}

	@Override
	public void bindToFilesystem()
	{
		final Map<String, Object> map = new HashMap<>();
		target.putObjectAsJson(map);
	}

	@Override
	public void bindToExternalFiles(final String absolutePath)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("alias", absolutePath);
		target.putObjectAsJson(map);
	}

	@Override
	public void delete()
	{
		target.delete();
	}

	@Override
	public boolean exists()
	{
		return target.exists(APPLICATION_JSON);
	}

	@Override
	public List<ListConfigItem> listLocations()
	{
		return target.withSegment(LOCATION).getJson(ListConfigResponse.class).childResources();
	}

	@Override
	public String url()
	{
		return target.url();
	}

	@Override
	public ManagementData data(final String path)
	{
		return new ManagementDataImpl(target.withSegment(DATA).withSlashSeparatedSegments(path));
	}

	@Override
	public ManagementData file(final String file)
	{
		return new ManagementDataImpl(target.withSegment(FILE).withSegment(file));
	}

	@Override
	public ManagementPathJavaPackage javaPackage(final String pkg)
	{
		return new ManagementPathImpl(target.withSegment(JAVA_PKG).withSegment(pkg));
	}

	@Override
	public ManagementDataSchema jsonSchema(final String schemaId)
	{
		return new ManagementDataImpl(target.withSegment(JSON_SCHEMA).withSegment(schemaId));
	}

	@Override
	public ManagementPathEntity entity(final String entityId)
	{
		return new ManagementPathImpl(target.withSegment(ENTITY).withSegment(entityId));
	}

	@Override
	public void putEntity(final String kindUrl, final String jsonSchemaUrl, final String javaUrl)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("kindUrl", kindUrl);
		map.put("jsonSchemaUrl", jsonSchemaUrl);
		map.put("javaUrl", javaUrl);
		target.putObjectAsJson(map);
	}

	@Override
	public ApiEntity getEntity()
	{
		return target.getJson(ApiEntity.class);
	}

	@Override
	public ApiComponent getComponent(final String componentId)
	{
		return target.withSegment(COMPONENT).withSegment(componentId).getJson(ApiComponent.class);
	}

	@Override
	public ResponseType getComponentResponseType(final String componentId)
	{
		return target.withSegment(COMPONENT).withSegment(componentId).getResponseType(APPLICATION_JSON);
	}

	@Override
	public List<String> listComponentIds()
	{
		return target.withSegment(COMPONENT)
				.getJson(ListComponentResponse.class)
				.childResources()
				.stream()
				.map(ListComponentItem::componentId)
				.collect(Collectors.toList());
	}

	@Override
	public ResponseType getEntityResponseType()
	{
		return target.getResponseType(APPLICATION_JSON);
	}

	@Override
	public ManagementPathKind kind(final String kindId)
	{
		return new ManagementPathImpl(target.withSegment("kind").withSegment(kindId));
	}

	@Override
	public void putJsonResource(final String resourcePath)
	{
		target.putResource(resourcePath, APPLICATION_JSON);
	}

	@Override
	public Kind getKind()
	{
		return target.getJson(Kind.class);
	}
}
