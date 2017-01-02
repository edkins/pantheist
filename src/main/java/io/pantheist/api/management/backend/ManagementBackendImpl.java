package io.pantheist.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import io.pantheist.api.management.model.ApiManagementModelFactory;
import io.pantheist.api.management.model.CreateConfigRequest;
import io.pantheist.api.management.model.ListConfigItem;
import io.pantheist.api.management.model.ListConfigResponse;
import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.nginx.manage.NginxService;
import io.pantheist.system.initializer.Initializer;

final class ManagementBackendImpl implements ManagementBackend
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementBackendImpl.class);
	private final FilesystemStore filesystem;
	private final ApiManagementModelFactory modelFactory;
	private final NginxService nginxService;
	private final UrlTranslation urlTranslation;
	private final Initializer initializer;
	private final CommonApiModelFactory commonFactory;

	@Inject
	ManagementBackendImpl(
			final FilesystemStore filesystem,
			final ApiManagementModelFactory modelFactory,
			final NginxService nginxService,
			final UrlTranslation urlTranslation,
			final KindStore kindStore,
			final Initializer initializer,
			final CommonApiModelFactory commonFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.modelFactory = checkNotNull(modelFactory);
		this.nginxService = checkNotNull(nginxService);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.initializer = checkNotNull(initializer);
		this.commonFactory = checkNotNull(commonFactory);
	}

	@Override
	public Possible<Void> putConfig(final String serverId, final String locationId, final CreateConfigRequest request)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.putAndRestart(port, locationId, Optional.ofNullable(request.alias()));
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> putData(final String path, final String data)
	{
		return filesystem.putSrvData(path, data);
	}

	@Override
	public Possible<String> getData(final String path)
	{
		return filesystem.getSrvData(path);
	}

	@Override
	public boolean configExists(final String serverId, final String locationId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.hasLocation(port, locationId);
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return false;
		}
	}

	@Override
	public Possible<Void> deleteConfig(final String serverId, final String locationId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			return nginxService.deleteLocationAndRestart(port, locationId);
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	private ListConfigItem makeListConfigItem(final String serverId, final String location)
	{
		return modelFactory.listConfigItem(urlTranslation.locationToUrl(serverId, location).toString());
	}

	@Override
	public Possible<ListConfigResponse> listLocations(final String serverId)
	{
		try
		{
			final int port = Integer.parseInt(serverId);
			final Possible<List<String>> locations = nginxService.listLocations(port);
			if (!locations.isPresent())
			{
				return locations.coerce();
			}
			final List<ListConfigItem> list = Lists.transform(locations.get(),
					loc -> makeListConfigItem(serverId, loc));
			return View.ok(modelFactory.listConfigResponse(list));
		}
		catch (final NumberFormatException e)
		{
			LOGGER.catching(e);
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public ListClassifierResponse listRootClassifiers()
	{
		return commonFactory.listClassifierResponse(urlTranslation.listRootClassifiers());
	}

	@Override
	public void reloadConfiguration()
	{
		initializer.reload();
	}

	@Override
	public void regenerateDb()
	{
		initializer.regenerateDb();
	}

	@Override
	public void scheduleTerminate()
	{
		initializer.stopAsync();
	}
}
