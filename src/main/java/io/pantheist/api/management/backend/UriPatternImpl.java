package io.pantheist.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.Escapers;
import io.pantheist.common.util.MutableOpt;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.View;

public class UriPatternImpl implements UriPattern
{
	private final String scheme;
	private final String authority;
	private final List<UriPatternSegment> segments;
	private final boolean allowTrailingSlash;

	private UriPatternImpl(final String scheme, final String authority,
			final List<UriPatternSegment> segments,
			final boolean allowTrailingSlash)
	{
		this.scheme = OtherPreconditions.checkNotNullOrEmpty(scheme);
		this.authority = OtherPreconditions.checkNotNullOrEmpty(authority);
		this.segments = checkNotNull(segments);
		this.allowTrailingSlash = allowTrailingSlash;
	}

	public static UriPattern hostAndPort(final String scheme, final String authority)
	{
		return new UriPatternImpl(scheme, authority, ImmutableList.of(), false);
	}

	private UriPattern patternSegment(final UriPatternSegment segment)
	{
		if (allowTrailingSlash)
		{
			throw new IllegalStateException("Can't chain more things after allowTrailingSlash");
		}
		if (segments.isEmpty() && !segment.isEmpty())
		{
			throw new IllegalStateException("Only empty segment can be appended as the first item after authority");
		}

		return AntiIt.from(segments)
				.append(segment)
				.wrap(xs -> new UriPatternImpl(scheme, authority, xs, false));
	}

	@Override
	public UriPattern allowTrailingSlash()
	{
		if (allowTrailingSlash)
		{
			throw new IllegalStateException("Can't chain more things after allowTrailingSlash");
		}
		return new UriPatternImpl(scheme, authority, segments, true);
	}

	@Override
	public UriPattern segment(final String segment)
	{
		return patternSegment(UriPatternSegmentLiteralImpl.of(segment));
	}

	@Override
	public UriPattern var(final String name)
	{
		return patternSegment(UriPatternSegmentVarImpl.named(name));
	}

	@Override
	public String getVar(final String name, final String url)
	{
		try
		{
			final URI uri = new URI(url);
			if (!scheme.equalsIgnoreCase(uri.getScheme()))
			{
				throw new UrlPatternMismatchException(
						"Wrong scheme. Should be " + scheme + ", was " + uri.getScheme() + " in " + url);
			}
			if (!authority.equals(uri.getAuthority()))
			{
				throw new UrlPatternMismatchException(
						"Wrong authority. Should be " + authority + ", was " + uri.getAuthority());
			}
			if (uri.getQuery() != null)
			{
				throw new UrlPatternMismatchException("Not expecting query component");
			}
			if (uri.getFragment() != null)
			{
				throw new UrlPatternMismatchException("Not expecting fragment component");
			}
			final List<String> path = AntiIt.split('/', uri.getPath()).toList();
			if (path.size() != segments.size())
			{
				if (path.size() == segments.size() + 1 && path.get(path.size() - 1).isEmpty())
				{
					// Handle special case of trailing slash
					if (!allowTrailingSlash)
					{
						throw new UrlPatternMismatchException("Trailing slash not allowed here");
					}
				}
				else
				{
					throw new UrlPatternMismatchException(
							"Wrong number of path segments. Should be " + segments.size() + ", was " + path.size());
				}
			}
			final MutableOpt<String> result = View.mutableOpt();
			for (int i = 0; i < segments.size(); i++)
			{
				final String pathSegment = Escapers.decodeUrl(path.get(i));
				if (!segments.get(i).matches(pathSegment))
				{
					throw new UrlPatternMismatchException(
							"Segment " + i + " " + segments.get(i) + " did not match " + pathSegment + " ["
									+ path.get(i)
									+ "]");
				}
				if (segments.get(i).name().equals(Optional.of(name)))
				{
					result.supply(pathSegment);
				}
			}
			return result.get();
		}
		catch (final URISyntaxException e)
		{
			throw new UrlPatternMismatchException(e);
		}
	}

	@Override
	public String generate(final Map<String, String> values)
	{
		return generate(values, false);
	}

	private String generate(final Map<String, String> values, final boolean trailingSlash)
	{
		final Map<String, String> mutableValues = new HashMap<>(values);
		if (trailingSlash && !allowTrailingSlash)
		{
			throw new IllegalArgumentException("trailingSlash requested when it is not allowed by this pattern");
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(scheme);
		sb.append("://");
		sb.append(authority);
		boolean first = true;
		for (final UriPatternSegment segment : segments)
		{
			if (!first)
			{
				sb.append('/');
			}
			first = false;
			sb.append(Escapers.url(segment.generateAndDelete(mutableValues)));
		}
		if (trailingSlash)
		{
			sb.append('/');
		}
		if (!mutableValues.isEmpty())
		{
			throw new IllegalArgumentException(
					"Variable name specified but was not in pattern: " + mutableValues.keySet());
		}
		return sb.toString();
	}

	@Override
	public UriPattern emptySegment()
	{
		return patternSegment(UriPatternSegmentEmptyImpl.EMPTY);
	}

	@Override
	public String template()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(scheme);
		sb.append("://");
		sb.append(authority);
		boolean first = true;
		for (final UriPatternSegment segment : segments)
		{
			if (!first)
			{
				sb.append('/');
			}
			first = false;
			sb.append(segment.template());
		}
		return sb.toString();
	}

}
