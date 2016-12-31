package restless.api.schema.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.CreateAction;
import restless.common.api.model.DataAction;
import restless.common.api.model.DeleteAction;

public interface ApiSchemaModelFactory
{
	ListSchemaResponse listSchemaResponse(
			List<ListSchemaItem> childResources,
			CreateAction createAction);

	ListSchemaItem listSchemaItem(@Assisted("url") String url);

	ApiSchema apiSchema(DataAction dataAction, DeleteAction deleteAction);
}
