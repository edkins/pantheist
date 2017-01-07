package io.pantheist.handler.plugin.backend;

import javax.inject.Inject;

import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.plugin.annotations.ChangeHook;
import io.pantheist.plugin.annotations.PantheistPlugin;
import io.pantheist.plugin.interfaces.AlteredSnapshot;

/**
 * This service takes care of registering plugins with the other handlers that are pluggable,
 * currently just {@link FileKindHandler}.
 */
public interface PluginHandler
{
	/**
	 * Register a class with the following annotations:
	 *
	 * {@link PantheistPlugin} on the class itself. Required.
	 *
	 * {@link Inject} on the constructor. Required on exactly one constructor.
	 * - currently we don't inject anything, but we might in the future.
	 *
	 * {@link ChangeHook} on at most one method, which must have exactly one parameter of type {@link AlteredSnapshot}
	 *
	 * A plugin class without any hook methods will be accepted here but won't be registered anywhere.
	 * Calling this method also deregisters any other plugin with the same id, so if you remove a hook
	 * from the class and then call this method with the new one, the old hook will disappear.
	 *
	 * All relevant methods and constructors, and the class itself, must be public. It obviously can't be abstract
	 * or an interface.
	 *
	 * @throws PluginHandlerException if the class doesn't meet these requirements.
	 */
	void registerPluginClass(String pluginId, Class<?> clazz);

	/**
	 * Deregister all plugins from all handlers, usually because you're doing a refresh and are
	 * about to add them back again.
	 */
	void deregisterAllPlugins();

	/**
	 * Broadcasts the fact that things might have changed. Used on initialization and refresh,
	 * after all the plugins have been registered.
	 */
	void sendGlobalChangeSignal();
}
