package restless.handler.parser.generic;

import java.util.List;

import javax.inject.Named;

import com.google.inject.assistedinject.Assisted;

public interface SyntaxSymbolFactory
{
	/**
	 * Creates a SyntaxSymbol matching the given regex.
	 */
	@Named("regex")
	SyntaxSymbol regex(@Assisted("name") String name, @Assisted("regex") String regex);

	@Named("many")
	SyntaxSymbol many(@Assisted("name") String name, SyntaxSymbol childSymbol);

	@Named("sequence")
	SyntaxSymbol sequence(@Assisted("name") String name, List<SyntaxSymbol> childSymbols);

	@Named("choice")
	SyntaxSymbol choice(@Assisted("name") String name, List<SyntaxSymbol> childSymbols);
}
