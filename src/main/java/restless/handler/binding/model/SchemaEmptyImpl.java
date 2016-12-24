package restless.handler.binding.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonCreator;

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

}
