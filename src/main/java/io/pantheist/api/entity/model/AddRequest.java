package io.pantheist.api.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = AddRequestImpl.class)
public interface AddRequest
{
	@JsonProperty("addName")
	String addName();
}
