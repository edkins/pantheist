package restless.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = KindImpl.class)
public interface Kind
{
	@JsonProperty("level")
	KindLevel level();

	@Nullable
	@JsonProperty("java")
	JavaClause java();
}
