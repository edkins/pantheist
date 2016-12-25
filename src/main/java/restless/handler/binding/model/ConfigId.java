package restless.handler.binding.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = PathSpecImpl.class)
public interface ConfigId
{
	@Override
	String toString();

	@Deprecated
	String nameHint();

	@Deprecated
	Binding emptyBinding();
}
