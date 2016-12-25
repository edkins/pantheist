package restless.handler.binding.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ConfigIdImpl.class)
public interface ConfigId
{
	@JsonValue
	@Override
	String toString();

	@Deprecated
	String nameHint();
}
