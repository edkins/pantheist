package restless.handler.kind.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface KindModelFactory
{
	Kind kind(KindLevel level, @Nullable JavaClause java);

	JavaClause javaClause(@Assisted("required") boolean required, @Nullable JavaKind javaKind);
}
