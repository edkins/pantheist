package restless.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import restless.common.util.AntiIt;
import restless.handler.nginx.parser.NginxDirective;

final class ConfigHelperLocationImpl implements ConfigHelperLocation
{
	private final NginxDirective directive;

	private ConfigHelperLocationImpl(final NginxDirective directive)
	{
		this.directive = checkNotNull(directive);
	}

	static ConfigHelperLocation of(final NginxDirective directive)
	{
		return new ConfigHelperLocationImpl(directive);
	}

	@Override
	public NginxDirective directive()
	{
		return directive;
	}

	@Override
	public String location()
	{
		return AntiIt.from(directive.parameters()).failIfMultiple().get();
	}

	@Override
	public void setAlias(final Optional<String> alias)
	{
		if (alias.isPresent())
		{
			directive.contents().getOrCreateSimple("alias").setSingleParameter(alias.get());
		}
		else
		{
			directive.contents().deleteAllByName("alias");
		}
	}

}
