package restless.api.management.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import restless.handler.binding.model.HandlerType;

final class HandlerRequestImpl implements HandlerRequest
{
	private final HandlerType handler;
	private final String handlerPath;

	HandlerRequestImpl(@JsonProperty("handler") final HandlerType handler,
			@Nullable @JsonProperty("handlerPath") final String handlerPath)
	{
		this.handler = checkNotNull(handler);
		this.handlerPath = handlerPath;
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
