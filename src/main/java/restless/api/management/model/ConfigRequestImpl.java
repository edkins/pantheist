package restless.api.management.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import restless.common.util.OtherPreconditions;
import restless.handler.binding.model.HandlerType;

final class ConfigRequestImpl implements ConfigRequest
{
	private final HandlerType handler;
	private final String handlerPath;

	ConfigRequestImpl(@JsonProperty("handler") final HandlerType handler,
			@JsonProperty("handlerPath") final String handlerPath)
	{
		this.handler = checkNotNull(handler);
		this.handlerPath = OtherPreconditions.checkNotNullOrEmpty(handlerPath);
	}

	@Override
	public HandlerType handler()
	{
		return handler;
	}

	@Override
	public String handlerPath()
	{
		return handlerPath;
	}

}
