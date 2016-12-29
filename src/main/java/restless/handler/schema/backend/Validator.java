package restless.handler.schema.backend;

import restless.common.util.AntiIterator;
import restless.common.util.Possible;
import restless.handler.schema.model.SchemaComponent;

interface Validator
{
	public Possible<Void> checkSchema();

	public Possible<Void> validate(String data);

	AntiIterator<SchemaComponent> components();
}
