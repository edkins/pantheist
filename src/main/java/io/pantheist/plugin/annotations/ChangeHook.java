package io.pantheist.plugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a plugin method which is to be called:
 * - when files associated with the relevant kinds change, appear or disappear
 * - on startup
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChangeHook
{

}
