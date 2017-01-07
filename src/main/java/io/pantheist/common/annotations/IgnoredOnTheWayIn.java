package io.pantheist.common.annotations;

/**
 * Used when:
 * - it's a field getter for an api object
 * - it's optional in the payload for PUT requests
 * - it's ignored in the payload for PUT requests
 *
 * May be used in combination with {@link javax.annotation.Nullable} and
 * {@link NotNullableOnTheWayOut}. If it isn't nullable, the assumption is
 * a dummy or empty one will be provided when you call the getter, if you
 * deserialize the parent object with a null or missing value.
 *
 * When this annotation is invoked, it explicitly says that the user can supply
 * (schematically valid) garbage data, most likely by receiving an object, tweaking some
 * things elsewhere that would render this invalid, and then sending it back.
 */
public @interface IgnoredOnTheWayIn
{

}
