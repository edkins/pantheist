package io.pantheist.api.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.ListClassifierResponse;

@JsonDeserialize(as = ListRootResponseImpl.class)
public interface ListRootResponse extends ListClassifierResponse
{
	@JsonProperty("clientConfigUrl")
	String clientConfigUrl();
}
