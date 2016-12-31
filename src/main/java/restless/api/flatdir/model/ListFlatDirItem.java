package restless.api.flatdir.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListFlatDirItemImpl.class)
public interface ListFlatDirItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("relativePath")
	String relativePath();
}
