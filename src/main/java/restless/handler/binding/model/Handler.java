package restless.handler.binding.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import restless.handler.filesystem.backend.FsPath;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = HandlerEmptyImpl.class, name = "empty"),
		@JsonSubTypes.Type(value = HandlerFilesystemImpl.class, name = "filesystem") })
public interface Handler
{
	HandlerType type();

	/**
	 * @throws UnsupportedOperationException if not a filesystem binding
	 */
	@JsonIgnore
	FsPath filesystemBucket();
}
