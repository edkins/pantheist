package restless.handler.schema.model;

import com.google.inject.assistedinject.Assisted;

public interface SchemaModelFactory
{
	SchemaComponent component(
			@Assisted("componentId") String componentId,
			@Assisted("isRoot") boolean isRoot);
}
