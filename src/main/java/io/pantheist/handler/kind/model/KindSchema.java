package io.pantheist.handler.kind.model;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = KindSchemaImpl.class)
public interface KindSchema
{
	@Nullable
	@JsonProperty("java")
	JavaClause java();

	@Nullable
	@JsonProperty("properties")
	Map<String, KindProperty> properties();

	/**
	 * How to identify an object as belonging to this kind.
	 *
	 * There is one special value: parentKind. Non-builtin kinds must specify this,
	 * and it's a string value referring to an existing kindId. Objects will first
	 * be identified as belonging to the parent kind, and then further differentiated
	 * according to the other requirements specified here.
	 *
	 * Multiple inheritance is not allowed at the moment. Something can't be its own parentKind,
	 * and any other kind of cycle is also disallowed (it won't cause the system any problems,
	 * at least for non-builtin kinds. It just means it'll never be matched).
	 *
	 * Any other requirements will correspond to an entry in the "properties" section
	 * of an ancestor kind.
	 *
	 * For example:
	 *
	 * "identification": {
	 *   "parentKind": "java-file",
	 *   "isInterface": true
	 * }
	 *
	 * For this to be matched, an object first has to be identified as a java-file. Then,
	 * the object's properties will be matched against those defined here. In this case,
	 * java files offer a boolean property called isInterface; only those with isInterface
	 * set to true will match this schema.
	 *
	 * This field is left as an undifferentiated ObjectNode because the different members
	 * will have different types.
	 *
	 * Generally speaking, an exact value for each property must be specified. In future
	 * the system might allow properties to be matched according to an expression (e.g. a regex)
	 * instead of an exact value. The "properties" declaration would specify the syntax to
	 * be used for matching that particular property.
	 *
	 * If a particular property is missing here, then any value for that property is allowed.
	 * If a property is listed here but doesn't exist in the parent kind (or ancestor kind, or
	 * possibly this kind itself), then this schema is considered invalid and nothing will match it.
	 *
	 * A missing or null value here is equivalent to an empty object, and can only be used for
	 * builtin kinds (because everything else must specify at least a parentKind).
	 *
	 * If you specify a parentKind and nothing else then you will automatically match everything
	 * belonging to the parent kind. This will cause problems if anything else has the same parentKind
	 * (and in general if something could match two child kinds, it's undefined which one will
	 * get used).
	 *
	 * Note that just because something is identified as your kind, doesn't mean it's a valid instance
	 * of it. There will be further levels of conformance checking later. Identification is
	 * more about saying "it looks more like this kind than anything else".
	 */
	@Nullable
	@JsonProperty("identification")
	ObjectNode identification();
}
