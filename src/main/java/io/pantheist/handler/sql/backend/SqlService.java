package io.pantheist.handler.sql.backend;

import java.util.List;

public interface SqlService
{
	void startOrRestart();

	void stop();

	List<String> listTableNames();
}
