package restless.handler.uri;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListClassifierItemImpl.class)
public interface ListClassifierItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("classifierSegment")
	String classifierSegment();
}
