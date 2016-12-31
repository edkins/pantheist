package io.pantheist.handler.schema.backend;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.schema.model.SchemaComponent;

interface Validator
{
	public Possible<Void> checkSchema();

	public Possible<Void> validate(String data);

	AntiIterator<SchemaComponent> components();
}
