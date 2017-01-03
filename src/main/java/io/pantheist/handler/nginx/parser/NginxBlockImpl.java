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

final class NginxBlockImpl implements NginxBlock
{
	private final List<NginxDirective> contents;
	private String delim1;
	private String delim2;
	private final NginxNodeFactory nodeFactory;
	private final String nlIndent; // newline character + amount of whitespace taking us to closing }
	private final String nlIndentInner; // newline character + amount of whitespace taking us to middle

	@Inject
	private NginxBlockImpl(
			final NginxNodeFactory nodeFactory,
			@Assisted("delim1") final String delim1,
			@Assisted final List<NginxDirective> contents,
			@Assisted("delim2") final String delim2)
	{
		this.nodeFactory = checkNotNull(nodeFactory);
		this.delim1 = checkNotNull(delim1);
		if (contents.isEmpty())
		{
			nlIndent = StringHelpers.nlIndent(delim1);
			if (delim2.isEmpty())
			{
				// we're the root block. No extra indentation needed.
				nlIndentInner = nlIndent;
			}
			else
			{
				nlIndentInner = nlIndent + "    ";
			}
		}
		else
		{
			nlIndentInner = StringHelpers.nlIndent(delim1);
			nlIndent = Make.last(contents).nlIndent();
		}
		checkNotNull(nlIndentInner);
		checkNotNull(nlIndent);
		this.contents = new ArrayList<>(contents);
		this.delim2 = checkNotNull(delim2);
	}

	@Override
	public StringBuilder toStringBuilder(final StringBuilder sb)
	{
		checkNotNull(delim1);
		checkNotNull(delim2);
		sb.append(delim1);
		contents.forEach(d -> d.toStringBuilder(sb));
		return sb.append(delim2);
	}

	@Override
	public String toString()
	{
		return toStringBuilder(new StringBuilder()).toString();
	}

	/**
	 * How much space is after things depends on what comes next.
	 *
	 * If the closing brace comes next, it's nlIndent. Otherwise it's nlIndentInner.
	 *
	 * This means when we add something to the end of the block, we need to update the
	 * whitespace on whatever came before, which is either the initial opening brace or
	 * another directive.
	 */
	private void addToEnd(final NginxDirective directive)
	{
		if (contents.isEmpty())
		{
			delim1 = StringHelpers.padTo(delim1, nlIndentInner);
		}
		else
		{
			Make.last(contents).padTo(nlIndentInner);
		}
		contents.add(directive);
	}

	@Override
	public NginxDirective addBlock(final String name, final List<String> parameters)
	{
		final NginxDirective block = createBlock(name, parameters);
		addToEnd(block);
		return block;
	}

	@Override
	public NginxDirective getOrCreateSimple(final String name)
	{
		return byName(name)
				.orElseGet(() -> {
					final NginxDirective item = createSimple(name);
					addToEnd(item);
					return item;
				});
	}

	@Override
	public NginxDirective getOrCreateBlock(final String name)
	{
		return byName(name)
				.orElseGet(() -> {
					final NginxDirective item = createBlock(name, new ArrayList<>());
					addToEnd(item);
					return item;
				});
	}

	private Optional<NginxDirective> byName(final String name)
	{
		return AntiIt.from(getAll(name)).failIfMultiple();
	}

	@Override
	public Optional<String> lookup(final String name)
	{
		return byName(name)
				.map(d -> AntiIt.from(d.parameters()).failIfMultiple().get());
	}

	@Override
	public boolean isEmpty()
	{
		return contents.isEmpty();
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

	private NginxDirective createSimple(final String name)
	{
		return nodeFactory.directive(nameAndParams(name, ImmutableList.of()),
				nodeFactory.noBlock(";" + nlIndent));
	}

	private NginxDirective createBlock(final String name, final List<String> params)
	{
		return nodeFactory.directive(nameAndParams(name, params),
				nodeFactory.block("{" + nlIndentInner, new ArrayList<>(), "}" + nlIndent));
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

	/**
	 * The outer block asked us to adjust our trailing padding
	 */
	@Override
	public void padTo(final String nlIndent)
	{
		delim2 = StringHelpers.padTo(delim2, nlIndent);
	}

	@Override
	public String nlIndent()
	{
		return StringHelpers.nlIndent(delim2);
	}
}
