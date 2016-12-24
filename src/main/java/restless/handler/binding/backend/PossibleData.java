package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;

public final class PossibleData
{
	private final Optional<String> data;
	private final WhatHappenedToData whatHappened;

	private PossibleData(final Optional<String> data, final WhatHappenedToData whatHappened)
	{
		this.data = checkNotNull(data);
		this.whatHappened = checkNotNull(whatHappened);
	}

	public static PossibleData of(final String data)
	{
		checkNotNull(data);
		return new PossibleData(Optional.of(data), WhatHappenedToData.OK);
	}

	public static PossibleData doesNotExist()
	{
		return new PossibleData(Optional.empty(), WhatHappenedToData.DOES_NOT_EXIST);
	}

	public static PossibleData handlerDoesNotSupport()
	{
		return new PossibleData(Optional.empty(), WhatHappenedToData.HANDLER_DOES_NOT_SUPPORT);
	}

	/**
	 * @throws WebApplicationException
	 */
	public String get()
	{
		if (data.isPresent())
		{
			return data.get();
		}
		else
		{
			throw new WebApplicationException(whatHappened.httpStatus);
		}
	}

	public boolean isPresent()
	{
		return data.isPresent();
	}

	public int httpStatus()
	{
		return whatHappened.httpStatus;
	}

	public boolean ok()
	{
		return whatHappened.equals(WhatHappenedToData.OK);
	}

	public static PossibleData requestFailedSchema()
	{
		return new PossibleData(Optional.empty(), WhatHappenedToData.REQUEST_FAILED_SCHEMA);
	}

	public static PossibleData alreadyExists()
	{
		return new PossibleData(Optional.empty(), WhatHappenedToData.ALREADY_EXISTS);
	}

	public static PossibleData requestHasInvalidSyntax()
	{
		return new PossibleData(Optional.empty(), WhatHappenedToData.REQUEST_HAS_INVALID_SYNTAX);
	}

	public PossibleEmpty thenEmpty(final Function<String, PossibleEmpty> fn)
	{
		if (ok())
		{
			return fn.apply(data.get());
		}
		else
		{
			return asEmpty();
		}
	}

	public PossibleEmpty asEmpty()
	{
		return PossibleEmpty.fromWhatHappend(whatHappened);
	}

	public String message()
	{
		return whatHappened.toString();
	}
}
