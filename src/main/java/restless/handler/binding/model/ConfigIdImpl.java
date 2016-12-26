package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;
import com.google.inject.assistedinject.Assisted;

final class ConfigIdImpl implements ConfigId
{
	private final String id;

	@Inject
	@JsonCreator
	private ConfigIdImpl(@Assisted final String id)
	{
		this.id = checkNotNull(id);
	}

	@Override
	public String toString()
	{
		return id;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof ConfigIdImpl)
		{
			final ConfigIdImpl that = (ConfigIdImpl) object;
			return Objects.equal(this.id, that.id);
		}
		return false;
	}

}
