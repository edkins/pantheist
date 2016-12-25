package restless.system.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.netflix.config.DynamicPropertyFactory;

import restless.common.annotations.NotFinalForTesting;

@VisibleForTesting
@NotFinalForTesting
public class RestlessConfigImpl implements RestlessConfig
{
	private final DynamicPropertyFactory propertyFactory;

	@Inject
	protected RestlessConfigImpl(final DynamicPropertyFactory propertyFactory)
	{
		this.propertyFactory = checkNotNull(propertyFactory);
	}

	@Override
	public int managementPort()
	{
		return propertyFactory.getIntProperty("RESTLESS_MANAGEMENT_PORT", 3301).get();
	}

	@Override
	public File dataDir()
	{
		final String path = propertyFactory.getStringProperty("RESTLESS_DATA_PATH", null).get();
		if (path == null)
		{
			return new File(System.getProperty("user.dir"), "data");
		}
		else
		{
			return new File(path);
		}
	}

	@Override
	public int mainPort()
	{
		return propertyFactory.getIntProperty("RESTLESS_MAIN_PORT", 3300).get();
	}

	@Override
	public String nginxExecutable()
	{
		return propertyFactory.getStringProperty("RESTLESS_NGINX", "/usr/sbin/nginx").get();
	}

	@Override
	public List<String> resourceFiles()
	{
		return ImmutableList.of("restless-frontend.js", "example-client.html");
	}

}
