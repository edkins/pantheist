package restless.handler.entity.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface EntityModelFactory
{
	Entity entity(@Nullable @Assisted("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted("javaPkg") final String javaPkg,
			@Nullable @Assisted("javaFile") String javaFile);
}
