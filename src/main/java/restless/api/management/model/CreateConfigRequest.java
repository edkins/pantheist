package restless.api.management.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = CreateConfigRequestImpl.class)
public interface CreateConfigRequest
{
	String pathSpec();
}
