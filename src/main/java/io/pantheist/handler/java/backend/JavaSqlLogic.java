package io.pantheist.handler.java.backend;

import io.pantheist.handler.java.model.JavaFileId;

interface JavaSqlLogic
{
	void update(JavaFileId id, String code);
}
