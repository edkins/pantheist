package restless.common.annotations;

import com.google.common.annotations.VisibleForTesting;

/**
 * Indicates that a class should be final, but was not made final in order to
 * help with testing.
 *
 * Usually goes with {@link VisibleForTesting}
 */
public @interface NotFinalForTesting
{

}
