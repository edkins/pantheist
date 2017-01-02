package io.pantheist.handler.kind.backend;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.kind.model.Entity;

public interface KindValidation
{
	Entity discoverJavaKind(JavaFileId javaFileId);

	AntiIterator<Entity> discoverEntitiesWithKind(String kindId);
}
