package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import restless.client.api.ManagementConfig;
import restless.client.api.ManagementData;
import restless.client.api.ManagementPath;
import restless.common.util.OtherPreconditions;

final class ManagementPathImpl implements ManagementPath
{
	private final TargetWrapper target;

	ManagementPathImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public ManagementPath segment(final String segment)
	{
		OtherPreconditions.checkNotNullOrEmpty(segment);
		return new ManagementPathImpl(target.withSegment("+" + segment));
	}

	@Override
	public ManagementData data(final String path)
	{
		return new ManagementDataImpl(target.withPlusEscapedSlashSeparatedSegments(path).withSegment("data"));
	}

	@Override
	public ManagementConfig config()
	{
		return new ManagementConfigImpl(target);
	}
}
