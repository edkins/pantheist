package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class BindingImpl implements Binding
{
	private final Handler handler;
	private final PathSpec pathSpec;
	private final Schema schema;

	@Inject
	BindingImpl(@Assisted @JsonProperty("handler") final Handler handler,
			@Assisted @JsonProperty("pathSpec") final PathSpec pathSpec,
			@Assisted @JsonProperty("schema") final Schema schema)
	{
		this.handler = checkNotNull(handler);
		this.pathSpec = checkNotNull(pathSpec);
		this.schema = checkNotNull(schema);
	}

	@Override
	public Handler handler()
	{
		return handler;
	}

	@Override
	public PathSpec pathSpec()
	{
		return pathSpec;
	}

	@Override
	public Schema schema()
	{
		return schema;
	}

}
