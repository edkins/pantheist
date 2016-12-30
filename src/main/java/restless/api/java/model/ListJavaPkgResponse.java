package restless.api.java.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListJavaPkgResponseImpl.class)
public interface ListJavaPkgResponse
{
	@JsonProperty("childResources")
	List<ListJavaPkgItem> childResources();
}
