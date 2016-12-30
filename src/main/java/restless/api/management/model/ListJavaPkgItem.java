package restless.api.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListJavaPkgItemImpl.class)
public interface ListJavaPkgItem
{
	@JsonProperty("url")
	String url();
}
