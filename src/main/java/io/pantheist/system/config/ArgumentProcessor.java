package io.pantheist.system.config;

import java.io.File;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
public interface ArgumentProcessor
{
	Optional<File> getConfigFile();
}
