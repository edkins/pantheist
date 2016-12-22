package restless.common.util;

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

}
