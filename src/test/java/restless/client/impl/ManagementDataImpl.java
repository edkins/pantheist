package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import restless.client.api.ManagementData;
import restless.client.api.ResponseType;

final class ManagementDataImpl implements ManagementData
{
	private final TargetWrapper target;

	ManagementDataImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public String getString()
	{
		return target.getTextPlain();
	}

	@Override
	public void putString(final String data)
	{
		target.putTextPlain(data);
	}

	@Override
	public ResponseType getResponseType()
	{
		return target.getTextPlainResponseType();
	}

}
