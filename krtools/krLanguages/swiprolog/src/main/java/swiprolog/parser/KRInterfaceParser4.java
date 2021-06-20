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

package swiprolog.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import swiprolog.validator.Validator4;
import swiprolog.visitor.Visitor4;

/**
 * Implementation of KR Tools {@link Parser} based on antlr4.
 */
public class KRInterfaceParser4 implements Parser {
	private final Validator4 validator;

	/**
	 * Creates a new KR interface parser that uses the given stream as input.
	 *
	 * @param r
	 *            The input stream.
	 * @param info
	 *            the {@link SourceInfo} for the fragment to be parsed. If set
	 *            to null, we use a default info object starting at line 1 with
	 *            a file reference set to null.
	 * @throws IOException
	 * @throws ParserException
	 *             If an exception occurred during parsing. See
	 *             {@link ParserException}.
	 */
	public KRInterfaceParser4(Reader r, SourceInfo info) throws IOException {
		this.validator = new Validator4(new Visitor4(new Parser4(r, info)));
	}

	@Override
	public Update parseUpdate() {
		return this.validator.updateOrEmpty();
	}

	@Override
	public List<DatabaseFormula> parseDBFs() {
		return this.validator.program();
	}

	@Override
	public List<Query> parseQueries() {
		return this.validator.goalSection();
	}

	@Override
	public Query parseQuery() {
		return this.validator.queryOrEmpty();
	}

	@Override
	public Var parseVar() {
		return this.validator.var();
	}

	@Override
	public Term parseTerm() {
		return this.validator.term();
	}

	@Override
	public List<Term> parseTerms() {
		return this.validator.terms();
	}

	@Override
	public List<SourceInfo> getErrors() {
		return new ArrayList<SourceInfo>(this.validator.getErrors());
	}

	@Override
	public List<SourceInfo> getWarnings() {
		return new ArrayList<>(0); // TODO
	}
}