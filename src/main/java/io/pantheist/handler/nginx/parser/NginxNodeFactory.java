package io.pantheist.handler.nginx.parser;

import java.util.List;

import javax.inject.Named;

import com.google.inject.assistedinject.Assisted;

interface NginxNodeFactory
{
	NginxWord word(
			@Assisted("word") final String word,
			@Assisted("ws") final String ws);

	NginxNameAndParameters nameAndParameters(
			@Assisted("name") final String name,
			@Assisted("ws") final String ws,
			final List<NginxWord> parameters);

	NginxDirective directive(
			NginxNameAndParameters nameAndParameters,
			NginxBlock block);

	@Named("empty")
	NginxBlock noBlock(@Assisted("delim") String delim);

	@Named("block")
	NginxBlock block(
			@Assisted("delim1") final String delim1,
			List<NginxDirective> contents,
			@Assisted("delim2") final String delim2);
}
