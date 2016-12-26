package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class HandlerImpl implements Handler
{
	private final HandlerType type;
	private final String handlerPath;

	@Inject
	HandlerImpl(@Assisted @JsonProperty("type") final HandlerType type,
			@Nullable @Assisted("handlerPath") @JsonProperty("handlerPath") final String handlerPath)
	{
		OtherPreconditions.nullIff(handlerPath,
				type != HandlerType.resource_files && type != HandlerType.external_files);

		this.type = checkNotNull(type);
		this.handlerPath = handlerPath;
	}

	@Override
	public HandlerType type()
	{
		return type;
	}

	@Override
	public String handlerPath()
	{
		return handlerPath;
	}
}
