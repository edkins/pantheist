package restless.system.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.inject.Inject;

import com.google.common.annotations.VisibleForTesting;
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
		return propertyFactory.getIntProperty("RESTLESS_MANAGEMENT_PORT", 3300).get();
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
	public String fsFilesystemPath()
	{
		return propertyFactory.getStringProperty("RESTLESS_FS_FILESYSTEM_PATH", "restless-filesystem").get();
	}

	@Override
	public String fsBindingPath()
	{
		return propertyFactory.getStringProperty("RESTLESS_FS_BINDING_PATH", "restless-binding").get();
	}

}
