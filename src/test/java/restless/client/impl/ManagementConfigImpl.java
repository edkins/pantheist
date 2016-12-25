package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import restless.client.api.ManagementConfig;
import restless.client.api.ManagementConfigPoint;

final class ManagementConfigImpl implements ManagementConfig
{
	private final TargetWrapper target;

	ManagementConfigImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public ManagementConfigPoint create(final String path)
	{
		final TargetWrapper newTarget = target.withPlusEscapedSlashSeparatedSegments(path).withSegment("*");
		return new ManagementConfigPointImpl(newTarget);
	}

}
