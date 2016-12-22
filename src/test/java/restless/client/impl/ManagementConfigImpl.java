package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import restless.client.api.ManagementConfig;

final class ManagementConfigImpl implements ManagementConfig
{
	private final TargetWrapper target;

	ManagementConfigImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public void bindToFilesystem(final String dataPath)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("handler", "filesystem");
		map.put("handlerPath", dataPath);
		target.putObjectAsJson(map);
	}

}
