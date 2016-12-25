package restless.handler.binding.backend;

import java.util.List;
import java.util.function.Function;

import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingMatch;
import restless.handler.binding.model.ConfigId;
import restless.handler.binding.model.PathSpec;

public interface BindingStore
{
	void changeConfig(ConfigId pathSpec, Function<Binding, Binding> fn);

	/**
	 * Return the binding at this exact path spec. Won't find things that are at a similar
	 * (i.e. overlapping but more general or specific) path.
	 *
	 * Will return an empty binding if there's nothing there.
	 */
	Binding exact(ConfigId pathSpec);

	/**
	 * Looks up the given path (which must be literal) and returns matching information or an empty match.
	 */
	BindingMatch lookup(PathSpec pathSpec);

	void initialize();

	List<Binding> snapshot();
}
