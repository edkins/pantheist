package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Supplier;

public class PossibleEmpty
{
	private final WhatHappenedToData whatHappened;

	private PossibleEmpty(final WhatHappenedToData whatHappened)
	{
		this.whatHappened = checkNotNull(whatHappened);
	}

	public static PossibleEmpty ok()
	{
		return new PossibleEmpty(WhatHappenedToData.NO_CONTENT);
	}

	public static PossibleEmpty doesNotExist()
	{
		return new PossibleEmpty(WhatHappenedToData.DOES_NOT_EXIST);
	}

	public static PossibleEmpty handlerDoesNotSupport()
	{
		return new PossibleEmpty(WhatHappenedToData.HANDLER_DOES_NOT_SUPPORT);
	}

	public static PossibleEmpty requestHasInvalidSyntax()
	{
		return new PossibleEmpty(WhatHappenedToData.REQUEST_HAS_INVALID_SYNTAX);
	}

	public static PossibleEmpty requestFailedSchema()
	{
		return new PossibleEmpty(WhatHappenedToData.REQUEST_FAILED_SCHEMA);
	}

	public int httpStatus()
	{
		return whatHappened.httpStatus;
	}

	public boolean isOk()
	{
		return whatHappened.equals(WhatHappenedToData.NO_CONTENT);
	}

	public PossibleEmpty then(final Runnable r)
	{
		if (isOk())
		{
			r.run();
		}
		return this;
	}

	public PossibleEmpty then(final Supplier<PossibleEmpty> r)
	{
		if (isOk())
		{
			return r.get();
		}
		return this;
	}
}
