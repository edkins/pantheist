package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class JavaClauseImpl implements JavaClause
{
	private final boolean required;
	private final JavaKind javaKind;

	@Inject
	JavaClauseImpl(
			@Assisted("required") @JsonProperty("required") final boolean required,
			@Nullable @Assisted @JsonProperty("javaKind") final JavaKind javaKind)
	{
		this.required = required;
		this.javaKind = javaKind;
	}

	@Override
	public boolean required()
	{
		return required;
	}

	@Override
	public JavaKind javaKind()
	{
		return javaKind;
	}

}
