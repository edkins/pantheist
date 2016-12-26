package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import restless.client.api.ManagementConfigPoint;
import restless.client.api.ManagementData;

final class ManagementConfigPointImpl implements ManagementConfigPoint
{
	private final TargetWrapper target;

	ManagementConfigPointImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public void bindToFilesystem()
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("type", "filesystem");
		target.withSegment("handler").putObjectAsJson(map);
	}

	@Override
	public void bindToResourceFiles(final String resourcePath)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("type", "resource_files");
		map.put("handlerPath", resourcePath);
		target.withSegment("handler").putObjectAsJson(map);
	}

	@Override
	public ManagementData schema()
	{
		return new ManagementDataImpl(target.withSegment("schema"));
	}

	@Override
	public ManagementData jerseyFile()
	{
		return new ManagementDataImpl(target.withSegment("jersey-file"));
	}
}
