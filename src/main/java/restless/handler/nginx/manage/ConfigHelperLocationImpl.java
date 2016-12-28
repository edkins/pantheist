package restless.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import restless.common.util.OptView;
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
		return directive.parameters().basic().list().failIfMultiple().get();
	}

	@Override
	public void setAlias(final OptView<String> alias)
	{
		if (alias.isPresent())
		{
			directive.contents().getOrCreateSimple("alias").parameters().setSingle(alias.get());
		}
		else
		{
			directive.contents().byName().deleteByKey("alias");
		}
	}

}
