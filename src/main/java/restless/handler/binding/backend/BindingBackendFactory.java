package restless.handler.binding.backend;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import restless.handler.binding.model.Binding;

interface BindingBackendFactory
{
	BindingSet bindingSet(List<Binding> bindings, @Assisted("counter") int counter);

	ManagementFunctions emptyManagementFunctions();
}
