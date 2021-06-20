/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package krTools.parser;

import java.util.List;

import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;

/**
 * Interface for a parser for the KR language. This class defines the
 * requirements that
 * <p>
 * Also see: {@link KRInterface#getParser(java.io.Reader)}.
 */
public interface Parser {

	/**
	 * Parses the input. Returns the (database) formulas found during parsing.
	 */
	List<DatabaseFormula> parseDBFs();

	/**
	 * Parses the input. Returns the queries found during parsing.
	 */
	List<Query> parseQueries();

	/**
	 * Parses the input. Returns the query found during parsing (possibly null).
	 */
	Query parseQuery();

	/**
	 * Parses the input. Returns the update found during parsing (possibly
	 * null).
	 */
	Update parseUpdate();

	/**
	 * Parses the input. Returns a variable obtained by parsing the input
	 * (possibly null).
	 */
	Var parseVar();

	/**
	 * Parses the input. Returns a term obtained by parsing the input (possibly
	 * null).
	 */
	Term parseTerm();

	/**
	 * Parses the input. Returns a list of terms obtained by parsing the input.
	 */
	List<Term> parseTerms();

	/**
	 * @return The list of all errors that occurred while parsing.
	 */
	List<SourceInfo> getErrors();

	/**
	 * @return The list of all warnings that were generated while parsing.
	 */
	List<SourceInfo> getWarnings();

}