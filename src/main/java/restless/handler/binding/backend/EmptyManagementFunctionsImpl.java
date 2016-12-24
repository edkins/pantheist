package restless.handler.binding.backend;

import javax.inject.Inject;

final class EmptyManagementFunctionsImpl implements ManagementFunctions
{
	@Inject
	private EmptyManagementFunctionsImpl()
	{

	}

	@Override
	public PossibleData getString()
	{
		return PossibleData.handlerDoesNotSupport();
	}

	@Override
	public PossibleEmpty putString(final String data)
	{
		return PossibleEmpty.handlerDoesNotSupport();
	}

}
