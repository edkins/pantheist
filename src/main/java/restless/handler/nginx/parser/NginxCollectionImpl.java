package restless.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import restless.common.util.MutableByKey;
import restless.common.util.MutableListView;
import restless.common.util.OptView;
import restless.common.util.OtherPreconditions;
import restless.common.util.View;

final class NginxCollectionImpl implements NginxCollection
{
	private final NginxNodeFactory nodeFactory;
	private final String nlIndent;

	// State
	private final MutableByKey<String, NginxDirective> contents;

	@Inject
	NginxCollectionImpl(final NginxNodeFactory nodeFactory,
			@Assisted final List<NginxDirective> list,
			@Assisted("nlIndent") final String nlIndent)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.nlIndent = OtherPreconditions.checkNotNullOrEmpty(nlIndent);
		this.contents = View.mutableCopy(list).organizeByKey(NginxDirective::name);
	}

	@Override
	public NginxDirective getOrCreateSimple(final String name)
	{
		return contents.getWithCreator(name, this::createSimple);
	}

	private NginxDirective createSimple(final String name)
	{
		return nodeFactory.directive(nameAndParams(name),
				nodeFactory.noBlock(";" + nlIndent));
	}

	@Override
	public NginxDirective getOrCreateBlock(final String name)
	{
		return contents.getWithCreator(name, this::createBlock);
	}

	private NginxDirective createBlock(final String name)
	{
		return nodeFactory.directive(nameAndParams(name),
				nodeFactory.block("{" + nlIndent, new ArrayList<>(), "}" + nlIndent));
	}

	private NginxNameAndParameters nameAndParams(final String name)
	{
		return nodeFactory.nameAndParameters(name, " ", new ArrayList<>());
	}

	@Override
	public OptView<String> lookup(final String name)
	{
		return contents.optGet(name).optMap(d -> d.parameters().basic().list().failIfMultiple());
	}

	@Override
	public NginxDirective addBlock(final String name)
	{
		final NginxDirective block = createBlock(name);
		contents.list().basic().insertAtEnd(block);
		return block;
	}

	@Override
	public MutableByKey<String, NginxDirective> byName()
	{
		return contents;
	}

	@Override
	public MutableListView<NginxDirective> list()
	{
		return contents.list();
	}
}
