package restless.handler.binding.model;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.assistedinject.Assisted;

public interface BindingModelFactory
{
	PathSpec pathSpec(List<PathSpecSegment> segments);

	ConfigId configId(String id);

	@Named("literal")
	PathSpecSegment literal(String value);

	@Named("multi")
	PathSpecSegment multi();

	Binding binding(
			PathSpec pathSpec,
			Handler handler,
			Schema schema,
			@Nullable @Assisted("jerseyClass") String jerseyClass,
			ConfigId configId);

	Handler handler(HandlerType type, @Nullable @Assisted("handlerPath") String handlerPath);

	@Named("empty")
	Schema emptySchema();

	@Named("json")
	Schema jsonSchema(JsonNode schema);

	BindingMatch match(PathSpecMatch pathMatch, Binding binding);

	PathSpecMatch pathSpecMatch(List<PathSpecMatchSegment> segments);
}
