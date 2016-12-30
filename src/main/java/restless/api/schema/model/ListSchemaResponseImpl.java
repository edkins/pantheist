package restless.api.schema.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ListSchemaResponseImpl implements ListSchemaResponse
{
	private final List<ListSchemaItem> childResources;

	@Inject
	private ListSchemaResponseImpl(@Assisted @JsonProperty("childResources") final List<ListSchemaItem> childResources)
	{
		this.childResources = checkNotNull(childResources);
	}

	@Override
	public List<ListSchemaItem> childResources()
	{
		return childResources;
	}

}
