package io.pantheist.common.annotations;

/**
 * Used when:
 * - it's a field getter for an api object
 * - it's marked {@link javax.annotation.Nullable}
 * - it's optional in the payload for PUT requests
 * - the system will always supply one in the response to GET requests
 *   (either individually or as part of a list)
 */
public @interface NotNullableOnTheWayOut
{

}
