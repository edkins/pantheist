package io.pantheist.handler.filekind.backend;

import io.pantheist.plugin.interfaces.AlteredSnapshot;

/**
 * This is the grand write operation.
 *
 * It's made a separate interface to avoid exposing it to the individual plugins,
 * which shouldn't be doing the grand write. After write() is called, this
 * AlteredSnapshot can't be used any more.
 */
public interface WriteableAlteredSnapshot extends AlteredSnapshot
{
	/**
	 * This writes out any files that have been changed.
	 *
	 * If a change hook has been added, that will also be called but only if any files
	 * have actually changed.
	 */
	void writeOutEverything();

	void forceChangeNotification();
}
