package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class BindingImpl implements Binding
{
	private final Handler handler;
	private final PathSpec pathSpec;
	private final Schema schema;
	private final String jerseyClass;

	@Inject
	BindingImpl(@Assisted @JsonProperty("handler") final Handler handler,
			@Assisted @JsonProperty("pathSpec") final PathSpec pathSpec,
			@Assisted @JsonProperty("schema") final Schema schema,
			@Nullable @Assisted("jerseyClass") @JsonProperty("jerseyClass") final String jerseyClass)
	{
		this.handler = checkNotNull(handler);
		this.pathSpec = checkNotNull(pathSpec);
		this.schema = checkNotNull(schema);
		this.jerseyClass = jerseyClass;
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

	@Override
	public String jerseyClass()
	{
		return jerseyClass;
	}

}
