package io.pantheist.handler.java.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = JavaFileIdImpl.class)
public interface JavaFileId
{
	@JsonProperty("pkg")
	String pkg();

	@JsonProperty("file")
	String file();

	@JsonIgnore
	String qualifiedName();
}
