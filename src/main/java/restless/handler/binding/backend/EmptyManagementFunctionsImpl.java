package restless.handler.binding.backend;

final class EmptyManagementFunctionsImpl implements ManagementFunctions
{
	EmptyManagementFunctionsImpl()
	{

	}

	@Override
	public PossibleData getString()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void putString(final String data)
	{
		throw new UnsupportedOperationException();
	}

}
