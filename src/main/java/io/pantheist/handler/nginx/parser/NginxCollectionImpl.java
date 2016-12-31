package io.pantheist.handler.nginx.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.Make;
import io.pantheist.common.util.OtherPreconditions;

final class NginxCollectionImpl implements NginxCollection
{
	private final NginxNodeFactory nodeFactory;
	private final String nlIndent;

	// State
	private final List<NginxDirective> contents;

	@Inject
	NginxCollectionImpl(final NginxNodeFactory nodeFactory,
			@Assisted final List<NginxDirective> contents,
			@Assisted("nlIndent") final String nlIndent)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.nlIndent = OtherPreconditions.checkNotNullOrEmpty(nlIndent);
		this.contents = contents;
	}

	@Override
	public NginxDirective getOrCreateSimple(final String name)
	{
		return byName(name)
				.orElseGet(() -> {
					final NginxDirective item = createSimple(name);
					contents.add(item);
					return item;
				});
	}

	private Optional<NginxDirective> byName(final String name)
	{
		return AntiIt.from(getAll(name)).failIfMultiple();
	}

	private NginxDirective createSimple(final String name)
	{
		return nodeFactory.directive(nameAndParams(name, ImmutableList.of()),
				nodeFactory.noBlock(";" + nlIndent));
	}

	@Override
	public NginxDirective getOrCreateBlock(final String name)
	{
		return byName(name)
				.orElseGet(() -> {
					final NginxDirective item = createBlock(name, new ArrayList<>());
					contents.add(item);
					return item;
				});
	}

	private NginxDirective createBlock(final String name, final List<String> params)
	{
		return nodeFactory.directive(nameAndParams(name, params),
				nodeFactory.block("{" + nlIndent, new ArrayList<>(), "}" + nlIndent));
	}

	private NginxNameAndParameters nameAndParams(final String name, final List<String> params)
	{
		final ArrayList<NginxWord> words = new ArrayList<>();
		if (!params.isEmpty())
		{
			for (int i = 0; i < params.size() - 1; i++)
			{
				words.add(nodeFactory.word(params.get(i), " "));
			}
			words.add(nodeFactory.word(Make.last(params), ""));
		}
		return nodeFactory.nameAndParameters(name, " ", words);
	}

	@Override
	public Optional<String> lookup(final String name)
	{
		return byName(name)
				.map(d -> AntiIt.from(d.parameters()).failIfMultiple().get());
	}

	@Override
	public NginxDirective addBlock(final String name, final List<String> parameters)
	{
		final NginxDirective block = createBlock(name, parameters);
		contents.add(block);
		return block;
	}

	@Override
	public List<NginxDirective> list()
	{
		return ImmutableList.copyOf(contents);
	}

	@Override
	public List<NginxDirective> getAll(final String name)
	{
		return contents.stream().filter(hasName(name)).collect(Collectors.toList());
	}

	private Predicate<NginxDirective> hasName(final String name)
	{
		OtherPreconditions.checkNotNullOrEmpty(name);
		return d -> name.equals(d.name());
	}

	@Override
	public boolean deleteAllByName(final String name)
	{
		return contents.removeIf(hasName(name));
	}

	@Override
	public boolean removeIf(final Predicate<NginxDirective> predicate)
	{
		return contents.removeIf(predicate);
	}
}
