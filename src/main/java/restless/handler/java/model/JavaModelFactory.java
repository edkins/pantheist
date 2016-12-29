package restless.handler.java.model;

import com.google.inject.assistedinject.Assisted;

public interface JavaModelFactory
{
	JavaComponent component(@Assisted("isRoot") boolean isRoot);
}
