package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Supplier;

public final class PossibleEmpty
{
	private final WhatHappenedToData whatHappened;

	private PossibleEmpty(final WhatHappenedToData whatHappened)
	{
		this.whatHappened = checkNotNull(whatHappened);
	}

	static PossibleEmpty fromWhatHappend(final WhatHappenedToData whatHappened)
	{
		return new PossibleEmpty(whatHappened);
	}

	public static PossibleEmpty ok()
	{
		return new PossibleEmpty(WhatHappenedToData.NO_CONTENT);
	}

	public static PossibleEmpty doesNotExist()
	{
		return new PossibleEmpty(WhatHappenedToData.DOES_NOT_EXIST);
	}

	public static PossibleEmpty parentDoesNotExist()
	{
		return new PossibleEmpty(WhatHappenedToData.PARENT_DOES_NOT_EXIST);
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

	public static PossibleEmpty alreadyExists()
	{
		return new PossibleEmpty(WhatHappenedToData.ALREADY_EXISTS);
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

	public String message()
	{
		return whatHappened.toString();
	}
}
