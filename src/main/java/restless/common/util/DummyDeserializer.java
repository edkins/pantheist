package restless.common.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * This is used in combination with {@link JacksonInject}, which otherwise has a terrible bug that
 * doesn't allow values to be injected properly. See
 *
 * https://github.com/FasterXML/jackson-databind/issues/962
 *
 */
public class DummyDeserializer extends JsonDeserializer<Object>
{

	@Override
	public Object deserialize(final JsonParser p, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException
	{
		throw new UnsupportedOperationException();
	}

}
