package restless.handler.entity.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface EntityModelFactory
{
	Entity entity(
			@Assisted("discovered") final boolean discovered,
			@Nullable @Assisted("kindId") final String kindId,
			@Nullable @Assisted("jsonSchemaId") final String jsonSchemaId,
			@Nullable @Assisted("javaPkg") final String javaPkg,
			@Nullable @Assisted("javaFile") String javaFile);
}
