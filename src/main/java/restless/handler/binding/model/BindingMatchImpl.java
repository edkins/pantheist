package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

final class BindingMatchImpl implements BindingMatch
{
	private final PathSpecMatch pathMatch;
	private final Binding binding;

	@Inject
	BindingMatchImpl(@Assisted final PathSpecMatch pathMatch,
			@Assisted final Binding binding)
	{
		this.pathMatch = checkNotNull(pathMatch);
		this.binding = checkNotNull(binding);
	}

	@Override
	public PathSpecMatch pathMatch()
	{
		return pathMatch;
	}

	@Override
	public Binding binding()
	{
		return binding;
	}

}
