package restless.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class KindImpl implements Kind
{
	private final KindLevel level;
	private final JavaClause java;

	@Inject
	private KindImpl(
			@Assisted @JsonProperty("level") final KindLevel level,
			@Nullable @Assisted @JsonProperty("java") final JavaClause java)
	{
		this.level = checkNotNull(level);
		this.java = java;
	}

	@Override
	public KindLevel level()
	{
		return level;
	}

	@Override
	public JavaClause java()
	{
		return java;
	}

}
