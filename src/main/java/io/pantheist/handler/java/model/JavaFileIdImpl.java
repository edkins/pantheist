package io.pantheist.handler.java.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class JavaFileIdImpl implements JavaFileId
{
	private final String pkg;
	private final String file;

	@Inject
	JavaFileIdImpl(
			@Assisted("pkg") @JsonProperty("pkg") final String pkg,
			@Assisted("file") @JsonProperty("file") final String file)
	{
		this.pkg = OtherPreconditions.checkNotNullOrEmpty(pkg);
		this.file = OtherPreconditions.checkNotNullOrEmpty(file);
	}

	@Override
	public String pkg()
	{
		return pkg;
	}

	@Override
	public String file()
	{
		return file;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("pkg", pkg)
				.add("file", file)
				.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(pkg, file);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof JavaFileIdImpl)
		{
			final JavaFileIdImpl that = (JavaFileIdImpl) object;
			return Objects.equal(this.pkg, that.pkg)
					&& Objects.equal(this.file, that.file);
		}
		return false;
	}

}
