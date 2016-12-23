package restless.api.management.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import restless.handler.binding.model.HandlerType;

final class ConfigRequestImpl implements ConfigRequest
{
	private final HandlerType handler;

	ConfigRequestImpl(@JsonProperty("handler") final HandlerType handler)
	{
		this.handler = checkNotNull(handler);
	}

	@Override
	public HandlerType handler()
	{
		return handler;
	}

}
