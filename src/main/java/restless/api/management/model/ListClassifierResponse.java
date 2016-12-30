package restless.api.management.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.common.api.model.ListClassifierItem;

/**
 * A "classifier" is one of the path segments that isn't the id of something,
 * and instead identifies the type of resource you're looking for.
 *
 * e.g. in /java-pkg/{pkg}/file/{file}
 * "java-pkg" and "file" are classifiers.
 *
 * This response lists them.
 *
 * The response will always be the same for a particular path, so this is
 * just to help the client out.
 *
 * ignoreUnknown is specified here to allow clients to read back a ListClassifierResponse
 * when the actual item might provide more information than that.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(as = ListClassifierResponseImpl.class)
public interface ListClassifierResponse
{
	@JsonProperty("childResources")
	List<? extends ListClassifierItem> childResources();
}
