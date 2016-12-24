package restless.handler.binding.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

final class SchemaEmptyImpl implements Schema
{
	@Inject
	@JsonCreator
	private SchemaEmptyImpl()
	{

	}

	@Override
	public SchemaType type()
	{
		return SchemaType.empty;
	}

	@Override
	public String httpContentType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String contentAsString()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JsonNode jsonNode()
	{
		throw new UnsupportedOperationException();
	}

}
