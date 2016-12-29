package restless.handler.kind.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface KindModelFactory
{
	Kind kind(
			@Nullable @Assisted("kindId") String kindId,
			KindLevel level,
			@Assisted("discoverable") Boolean discoverable,
			@Nullable JavaClause java);

	JavaClause javaClause(@Assisted("required") boolean required, @Nullable JavaKind javaKind);
}
