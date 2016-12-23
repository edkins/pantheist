package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import restless.common.util.OtherPreconditions;
import restless.handler.binding.model.HandlerType;
import restless.handler.binding.model.PathSpec;

final class BindingImpl implements Binding
{
	private final HandlerType handler;
	private final PathSpec pathSpec;
	private final String bindingId;
	private final String handlerPath;

	BindingImpl(@JsonProperty("handler") final HandlerType handler,
			@JsonProperty("pathSpec") final PathSpec pathSpec,
			@JsonProperty("bindingId") final String bindingId,
			@JsonProperty("handlerPath") final String handlerPath)
	{
		this.handler = checkNotNull(handler);
		this.pathSpec = checkNotNull(pathSpec);
		this.bindingId = OtherPreconditions.checkNotNullOrEmpty(bindingId);
		this.handlerPath = handlerPath;
	}

	@Override
	public HandlerType handler()
	{
		return handler;
	}

	@Override
	public PathSpec pathSpec()
	{
		return pathSpec;
	}

	@Override
	public String bindingId()
	{
		return bindingId;
	}

	@Override
	public String handlerPath()
	{
		return handlerPath;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(handler, pathSpec, bindingId, handlerPath);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof BindingImpl)
		{
			final BindingImpl that = (BindingImpl) object;
			return Objects.equal(this.handler, that.handler)
					&& Objects.equal(this.pathSpec, that.pathSpec)
					&& Objects.equal(this.bindingId, that.bindingId)
					&& Objects.equal(this.handlerPath, that.handlerPath);
		}
		return false;
	}

}
