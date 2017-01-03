package io.pantheist.common.shared.model;

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.assistedinject.Assisted;

public interface CommonSharedModelFactory
{
	GenericProperty property(
			@Assisted("name") String name,
			TypeInfo typeInfo,
			Map<String, TypeInfo> elementProperties);

	@Named("boolean")
	GenericPropertyValue booleanValue(
			@Assisted("name") String name,
			@Assisted("value") boolean value);

	@Named("string")
	GenericPropertyValue stringValue(
			@Assisted("name") String name,
			@Assisted("value") String value);

	@Named("stringArray")
	GenericPropertyValue stringArrayValue(
			@Assisted("name") String name,
			@Assisted("value") List<String> value);

	@Named("objectArray")
	GenericPropertyValue objectArrayValue(
			@Assisted("name") String name,
			TypeInfo typeInfo,
			ArrayNode value);

	TypeInfo typeInfo(
			PropertyType type,
			Map<String, TypeInfo> itemProperties);
}
