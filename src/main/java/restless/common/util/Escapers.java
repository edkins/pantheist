package restless.common.util;

import static restless.common.util.OtherPreconditions.checkNotNullOrEmpty;

import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriComponent.Type;

public class Escapers
{
	private Escapers()
	{
		throw new UnsupportedOperationException();
	}

	public static String url(final String segment)
	{
		checkNotNullOrEmpty(segment);
		if (segment.equals("."))
		{
			return "%2E";
		}
		else if (segment.equals(".."))
		{
			return "%2E%2E";
		}
		else
		{
			return UriComponent.encode(segment, Type.PATH_SEGMENT);
		}
	}

	public static String decodeUrl(final String encoded)
	{
		checkNotNullOrEmpty(encoded);
		return UriComponent.decode(encoded, Type.PATH_SEGMENT);
	}
}
