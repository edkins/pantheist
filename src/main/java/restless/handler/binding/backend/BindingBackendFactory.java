package restless.handler.binding.backend;

import java.util.List;

import restless.handler.binding.model.Binding;

interface BindingBackendFactory
{
	BindingSet bindingSet(List<Binding> bindings);

	ManagementFunctions emptyManagementFunctions();
}
