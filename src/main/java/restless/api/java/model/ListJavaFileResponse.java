package restless.api.java.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListJavaFileResponseImpl.class)
public interface ListJavaFileResponse
{
	@JsonProperty("childResources")
	List<ListJavaFileItem> childResources();
}
