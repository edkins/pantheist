package io.pantheist.system.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;

public class SystemInjectModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ObjectMapper.class);
	}

	@Singleton
	@Provides
	ObjectMapper providesObjectMapper(final Injector injector)
	{
		final ObjectMapper objectMapper = new ObjectMapper();
		//final InjectableValues injectableValues = new InjectableValues.Std()
		//		.addValue(ObjectMapper.class, objectMapper);
		final InjectableValues injectableValues = new GuiceInjectableValues(injector);
		objectMapper.setInjectableValues(injectableValues);
		return objectMapper;
	}

	private static class GuiceInjectableValues extends InjectableValues
	{
		private final Injector injector;

		private GuiceInjectableValues(final Injector injector)
		{
			this.injector = checkNotNull(injector);
		}

		@Override
		public Object findInjectableValue(final Object valueId, final DeserializationContext ctxt,
				final BeanProperty forProperty,
				final Object beanInstance)
		{
			try
			{
				return injector.getInstance(Class.forName((String) valueId));
			}
			catch (final ClassNotFoundException e)
			{
				throw Throwables.propagate(e);
			}
		}
	}
}
