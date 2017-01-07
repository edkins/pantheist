package io.pantheist.common.util;

public enum FailureReason
{
	OK(200),
	NO_CONTENT(204),
	DOES_NOT_EXIST(404),
	PARENT_DOES_NOT_EXIST(404),
	BAD_LOCATION(404),
	ALREADY_EXISTS(409),
	KIND_DOES_NOT_SUPPORT(400),
	REQUEST_HAS_INVALID_SYNTAX(400),
	REQUEST_FAILED_SCHEMA(400),
	REQUEST_INVALID_OPERATION(400),
	KIND_IS_INVALID(400),
	OPERATING_ON_INVALID_ENTITY(400),
	WRONG_LOCATION(400),
	UNSUPPORTED_MEDIA_TYPE(415),
	MISCONFIGURED(500),
	IO_PROBLEM(500);

	final int httpStatus;

	private FailureReason(final int httpStatus)
	{
		this.httpStatus = httpStatus;
	}

	public int httpStatus()
	{
		return httpStatus;
	}

	/**
	 * Return an object indicating that this failure happened. Will be empty.
	 */
	public <T> Possible<T> happened()
	{
		return PossibleFailureImpl.of(this);
	}
}