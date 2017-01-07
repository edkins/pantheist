package io.pantheist.handler.plugin.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.pantheist.handler.filekind.backend.FileKindHandler;
import io.pantheist.plugin.annotations.ChangeHook;
import io.pantheist.plugin.interfaces.AlteredSnapshot;

final class PluginHandlerImpl implements PluginHandler
{
	private final FileKindHandler fileKindHandler;

	@Inject
	private PluginHandlerImpl(final FileKindHandler fileKindHandler)
	{
		this.fileKindHandler = checkNotNull(fileKindHandler);
	}

	@Override
	public void registerPluginClass(final String pluginId, final Class<?> clazz)
	{
		basicSanityChecks(clazz);

		final Object plugin;
		try
		{
			plugin = injectableConstructor(clazz).newInstance();
			checkNotNull(plugin); // I don't think this would happen
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new PluginHandlerException(e);
		}

		fileKindHandler.deregisterPlugin(pluginId);

		final Optional<Consumer<AlteredSnapshot>> changeHook = changeHook(clazz, plugin);
		if (changeHook.isPresent())
		{
			fileKindHandler.registerChangeHookPlugin(pluginId, changeHook.get());
		}
	}

	private Optional<Consumer<AlteredSnapshot>> changeHook(final Class<?> clazz, final Object plugin)
	{
		Optional<Consumer<AlteredSnapshot>> result = Optional.empty();
		for (final Method method : clazz.getMethods())
		{
			if (method.getAnnotation(ChangeHook.class) != null)
			{
				if (result.isPresent())
				{
					throw new PluginHandlerException(
							"Multiple methods marked with @" + ChangeHook.class.getName());
				}

				if (method.getParameterTypes().length != 1
						|| !method.getParameterTypes()[0].equals(AlteredSnapshot.class))
				{
					throw new PluginHandlerException(
							"@ChangeHook must have exactly one parameter, of type " + AlteredSnapshot.class.getName());
				}

				result = Optional.of(snapshot -> {
					try
					{
						method.invoke(plugin, snapshot);
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						throw new PluginHandlerException(e);
					}
				});
			}
		}
		return result;
	}

	private Constructor<?> injectableConstructor(final Class<?> clazz)
	{
		Optional<Constructor<?>> injectableConstructor = Optional.empty();
		for (final Constructor<?> constructor : clazz.getConstructors())
		{
			if (constructor.getAnnotation(Inject.class) != null)
			{
				if (injectableConstructor.isPresent())
				{
					throw new PluginHandlerException("Multiple constructors marked with @" + Inject.class.getName());
				}

				if (constructor.getParameterCount() != 0)
				{
					throw new PluginHandlerException("Currently can't inject anything into plugin constructor");
				}

				injectableConstructor = Optional.of(constructor);
			}
		}
		if (!injectableConstructor.isPresent())
		{
			throw new PluginHandlerException("No constructor marked with @javax.inject.Inject");
		}
		return injectableConstructor.get();
	}

	private void basicSanityChecks(final Class<?> clazz)
	{
		checkNotNull(clazz);
		if (clazz.isInterface())
		{
			throw new PluginHandlerException("It's an interface");
		}

		if (clazz.isAnnotation())
		{
			throw new PluginHandlerException("It's an annotation");
		}

		if (clazz.isEnum())
		{
			throw new PluginHandlerException("It's an enum");
		}

		if (clazz.isPrimitive())
		{
			throw new PluginHandlerException("It's primitive");
		}

		if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0)
		{
			throw new PluginHandlerException("It's abstract");
		}

		if ((clazz.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC)
		{
			throw new PluginHandlerException("It's not public");
		}

		if (clazz.getTypeParameters().length != 0)
		{
			throw new PluginHandlerException("Can't deal with type parameters");
		}
	}

	@Override
	public void deregisterAllPlugins()
	{
		fileKindHandler.deregisterAllPlugins();
	}

	@Override
	public void sendGlobalChangeSignal()
	{
		fileKindHandler.sendGlobalChangeSignal();
	}

}
