package restless.client.impl;

interface TargetRoot
{
	TargetWrapper home();

	TargetWrapper forUri(String uri);
}
