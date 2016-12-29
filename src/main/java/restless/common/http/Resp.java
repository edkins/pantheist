package restless.common.http;

import java.io.IOException;

import javax.ws.rs.core.Response;

import restless.common.util.FailureReason;
import restless.common.util.Possible;

public interface Resp
{

	<T> Response possibleToJson(Possible<T> result);

	Response jsonValidation(IOException e);

	Response unexpectedError(Exception ex);

	Response possibleData(Possible<String> data);

	Response possibleEmpty(Possible<Void> data);

	Response failure(FailureReason fail);

}
