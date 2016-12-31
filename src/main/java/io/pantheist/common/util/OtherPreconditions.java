package io.pantheist.common.util;

public class OtherPreconditions
{
	private OtherPreconditions()
	{
		throw new UnsupportedOperationException();
	}

	public static String checkNotNullOrEmpty(final String arg)
	{
		if (arg == null)
		{
			throw new NullPointerException("Argument is null");
		}
		if (arg.isEmpty())
		{
			throw new IllegalArgumentException("Argument is empty");
		}
		return arg;
	}

	public static void nullIff(final String handlerPath, final boolean shouldBeNull)
	{
		if ((handlerPath == null) != shouldBeNull)
		{
			throw new IllegalArgumentException("Wrong nullness");
		}
	}

	public static void checkNonNegative(final long n)
	{
		if (n < 0)
		{
			throw new IllegalArgumentException("Argument is negative");
		}
	}
}
