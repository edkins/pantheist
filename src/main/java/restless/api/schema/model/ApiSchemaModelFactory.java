package restless.api.schema.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface ApiSchemaModelFactory
{
	ListSchemaResponse listSchemaResponse(List<ListSchemaItem> childResources);

	ListSchemaItem listSchemaItem(@Assisted("url") String url);
}
