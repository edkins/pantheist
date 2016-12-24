package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

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
}
