package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import restless.client.api.ManagementConfigPoint;
import restless.client.api.ManagementData;
import restless.client.api.ManagementUnexpectedResponseException;
import restless.client.api.ResponseType;

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
	public void bindToExternalFiles(final String absolutePath)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("type", "external_files");
		map.put("handlerPath", absolutePath);
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

	@Override
	public boolean exists()
	{
		final ResponseType responseType = target.getResponseType("application/json");
		switch (responseType) {
		case OK:
			return true;
		case NOT_FOUND:
			return false;
		default:
			throw new ManagementUnexpectedResponseException(responseType);
		}
	}

	@Override
	public void delete()
	{
		target.delete();
	}

	@Override
	public String url()
	{
		return target.url();
	}
}
