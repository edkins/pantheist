package restless.handler.entity.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import restless.handler.java.model.JavaFileId;

public interface EntityModelFactory
{
	Entity entity(
			@Assisted("entityId") String entityId,
			@Assisted("discovered") boolean discovered,
			@Nullable @Assisted("kindId") String kindId,
			@Nullable @Assisted("jsonSchemaId") String jsonSchemaId,
			@Nullable JavaFileId javaFileId);
}
