package restless.handler.binding.backend;

enum WhatHappenedToData
{
	OK(200),
	NO_CONTENT(204),
	DOES_NOT_EXIST(404),
	PARENT_DOES_NOT_EXIST(404),
	ALREADY_EXISTS(409),
	HANDLER_DOES_NOT_SUPPORT(501),
	REQUEST_HAS_INVALID_SYNTAX(400),
	REQUEST_FAILED_SCHEMA(400);

	final int httpStatus;

	private WhatHappenedToData(final int httpStatus)
	{
		this.httpStatus = httpStatus;
	}
}