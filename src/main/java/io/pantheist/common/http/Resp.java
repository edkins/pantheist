package io.pantheist.common.http;

import javax.ws.rs.core.Response;

import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;

public interface Resp
{
	<T> Response possibleToJson(Possible<T> result);

	<T> Response toJson(T result);

	Response unexpectedError(Exception ex);

	Response possibleData(Possible<String> data);

	Response possibleEmpty(Possible<Void> data);

	Response failure(FailureReason fail);

	<T> Possible<T> request(String requestJson, Class<T> clazz);
}
