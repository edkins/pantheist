package io.pantheist.handler.java.backend;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Pair;
import io.pantheist.handler.java.model.JavaFileId;

interface JavaSqlLogic
{
	void update(AntiIterator<Pair<JavaFileId, String>> codeWithIds);
}
