package io.pantheist.testclient.impl;

interface TargetRoot
{
	TargetWrapper home();

	TargetWrapper forUri(String uri);
}
