package restless.handler.schema.backend;

import restless.common.util.Possible;

interface Validator
{
	public Possible<Void> checkSchema();

	public Possible<Void> validate(String data);
}