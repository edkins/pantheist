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
import restless.client.api.ManagementPathLocation;
import restless.client.api.ManagementPathRoot;
import restless.client.api.ManagementPathServer;
import restless.client.api.ResponseType;

final class ManagementPathImpl implements
		ManagementPathServer,
		ManagementPathLocation,
		ManagementPathRoot,
		ManagementPathJavaPackage,
		ManagementPathEntity
{
	private final TargetWrapper target;

	ManagementPathImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public ManagementPathServer server(final int port)
	{
		return new ManagementPathImpl(target.withSegment("server").withSegment(String.valueOf(port)));
	}

	@Override
	public ManagementPathLocation location(final String path)
	{
		return new ManagementPathImpl(target.withSegment("location").withEscapedSegment(path));
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
		return target.exists("application/json");
	}

	@Override
	public List<ListConfigItem> listLocations()
	{
		return target.withSegment("location").getJson(ListConfigResponse.class).childResources();
	}

	@Override
	public String url()
	{
		return target.url();
	}

	@Override
	public ManagementData data(final String path)
	{
		return new ManagementDataImpl(target.withSegment("data").withSlashSeparatedSegments(path));
	}

	@Override
	public ManagementData file(final String file)
	{
		return new ManagementDataImpl(target.withSegment("file").withSegment(file));
	}

	@Override
	public ManagementPathJavaPackage javaPackage(final String pkg)
	{
		return new ManagementPathImpl(target.withSegment("java-pkg").withSegment(pkg));
	}

	@Override
	public ManagementDataSchema jsonSchema(final String schemaId)
	{
		return new ManagementDataImpl(target.withSegment("json-schema").withSegment(schemaId));
	}

	@Override
	public ManagementPathEntity entity(final String entityId)
	{
		return new ManagementPathImpl(target.withSegment("entity").withSegment(entityId));
	}

	@Override
	public void putEntity(final String jsonSchemaUrl, final String javaUrl)
	{
		final Map<String, Object> map = new HashMap<>();
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
		return target.withSegment("component").withSegment(componentId).getJson(ApiComponent.class);
	}

	@Override
	public ResponseType getComponentResponseType(final String componentId)
	{
		return target.withSegment("component").withSegment(componentId).getResponseType("application/json");
	}

	@Override
	public List<String> listComponentIds()
	{
		return target.withSegment("component")
				.getJson(ListComponentResponse.class)
				.childResources()
				.stream()
				.map(ListComponentItem::componentId)
				.collect(Collectors.toList());
	}
}
