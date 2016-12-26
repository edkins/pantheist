package restless.handler.binding.backend;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import restless.handler.binding.model.Binding;
import restless.handler.binding.model.BindingMatch;
import restless.handler.binding.model.ConfigId;
import restless.handler.binding.model.PathSpec;

public interface BindingStore
{
	void changeConfig(ConfigId pathSpec, Function<Binding, Binding> fn);

	boolean has(ConfigId configId);

	/**
	 * Return the binding at this exact path spec. Won't find things that are at a similar
	 * (i.e. overlapping but more general or specific) path.
	 *
	 * Will return an empty binding if there's nothing there.
	 */
	Binding exact(ConfigId pathSpec);

	/**
	 * Looks up the given path (which must be literal) and returns matching information or an empty result if no match.
	 */
	Optional<BindingMatch> lookup(PathSpec pathSpec);

	void initialize();

	List<Binding> listBindings();

	ConfigId createConfig(PathSpec pathSpec);

	/**
	 * Deletes the config with the given id. Return true if successfully deleted, false if
	 * it did not exist.
	 */
	boolean deleteConfig(ConfigId configId);
}
