package restless.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = KindImpl.class)
public interface Kind
{
	@Nullable
	@JsonProperty("kindId")
	String kindId(); // optional on put requests, but if present must agree with where you're putting it.

	@JsonProperty("level")
	KindLevel level();

	@JsonProperty("discoverable")
	boolean discoverable();

	@Nullable
	@JsonProperty("java")
	JavaClause java();
}
