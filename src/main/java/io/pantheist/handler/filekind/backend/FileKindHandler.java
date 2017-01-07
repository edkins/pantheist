package io.pantheist.handler.filekind.backend;

import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.plugin.interfaces.AlteredSnapshot;

public interface FileKindHandler
{
	/**
	 * @return a url, or KIND_DOES_NOT_SUPPORT
	 */
	Possible<String> newInstanceOfKind(Kind kind);

	AntiIterator<ObjectNode> discoverFileEntities(Kind kind);

	Possible<KindedMime> getEntity(Kind kind, String entityId);

	Possible<Void> add(Kind kind, String entityId, String addName);

	Possible<Void> putEntity(Kind kind, String entityId, String text, boolean failIfExists);

	Possible<Void> deleteEntity(Kind kind, String entityId);

	void deregisterAllPlugins();

	void registerChangeHookPlugin(String pluginId, Consumer<AlteredSnapshot> pluginHook);

	void deregisterPlugin(String pluginId);

	Optional<Kind> getKind(String kindId);

	AntiIterator<String> listAllEntityTexts(Kind kind);

	void sendGlobalChangeSignal();

	Kind metakind();

	AntiIterator<Kind> listAllKinds();
}
