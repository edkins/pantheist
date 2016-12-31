package io.pantheist.api.java.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;

/**
 * What you get when you request information about a java file.
 *
 * This response does not include the file data itself, but it tells you
 * that there is data available.
 */
@JsonDeserialize(as = ApiJavaFileImpl.class)
public interface ApiJavaFile
{
	@JsonProperty("dataAction")
	DataAction dataAction();

	@JsonProperty("deleteAction")
	DeleteAction deleteAction();

	@Nullable
	@JsonProperty("kindUrl")
	String kindUrl();
}
