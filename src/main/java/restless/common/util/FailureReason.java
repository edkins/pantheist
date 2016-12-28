package restless.common.util;

public enum FailureReason
{
	OK(200),
	NO_CONTENT(204),
	DOES_NOT_EXIST(404),
	PARENT_DOES_NOT_EXIST(404),
	BAD_LOCATION(404),
	ALREADY_EXISTS(409),
	HANDLER_DOES_NOT_SUPPORT(501),
	REQUEST_HAS_INVALID_SYNTAX(400),
	REQUEST_FAILED_SCHEMA(400),
	MISCONFIGURED(500);

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