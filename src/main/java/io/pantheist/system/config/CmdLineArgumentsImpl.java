package io.pantheist.system.config;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

final class CmdLineArgumentsImpl implements CmdLineArguments
{
	private final List<String> args;

	@Inject
	private CmdLineArgumentsImpl(@CmdLineArgumentArray final String[] argArray)
	{
		this.args = ImmutableList.copyOf(argArray);
	}

	@Override
	public List<String> args()
	{
		return args;
	}

}
