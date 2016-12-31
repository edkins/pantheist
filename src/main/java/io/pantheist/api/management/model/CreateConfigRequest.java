package io.pantheist.api.management.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = CreateConfigRequestImpl.class)
public interface CreateConfigRequest
{
	@Nullable
	String alias();
}
