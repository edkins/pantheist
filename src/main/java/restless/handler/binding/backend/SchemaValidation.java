package restless.handler.binding.backend;

import restless.handler.binding.model.Schema;

public interface SchemaValidation
{
	/**
	 * @return empty if whether the schema itself is valid, otherwise an error code
	 */
	PossibleEmpty checkSchema(Schema schema);

	/**
	 * @return empty if the data satisfies the schema, otherwise an error code
	 */
	PossibleEmpty validate(Schema schema, String data);
}
