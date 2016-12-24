package restless.handler.binding.model;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.assistedinject.Assisted;

import restless.handler.filesystem.backend.FsPath;

public interface BindingModelFactory
{
	PathSpec pathSpec(List<PathSpecSegment> segments);

	@Named("literal")
	PathSpecSegment literal(String value);

	@Named("star")
	PathSpecSegment star();

	@Named("multi")
	PathSpecSegment multi();

	Binding binding(
			PathSpec pathSpec,
			Handler handler,
			Schema schema,
			@Nullable @Assisted("jerseyClass") String jerseyClass);

	@Named("empty")
	Handler emptyHandler();

	@Named("filesystem")
	Handler filesystem(FsPath bucket);

	@Named("empty")
	Schema emptySchema();

	@Named("json")
	Schema jsonSchema(JsonNode schema);

	BindingMatch match(PathSpecMatch pathMatch, Binding binding);

	PathSpecMatch pathSpecMatch(List<PathSpecMatchSegment> segments);
}
