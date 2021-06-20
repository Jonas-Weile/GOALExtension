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

package tuprolog.language;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alice.tuprolog.Term;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.parser.SourceInfo;
import tuprolog.parser.PrologOperators;

/**
 * Analyzer to identify unused and undefined predicates.
 */
public class Analyzer {
	private static final Term ANON_VAR = new alice.tuprolog.Var("_");
	/**
	 * Map of definitions.
	 */
	private final Map<String, List<PrologDBFormula>> definitions = new LinkedHashMap<>();
	/**
	 * Map of queries.
	 */
	private final Map<String, List<PrologQuery>> used = new LinkedHashMap<>();
	/**
	 * Input
	 */
	private final Set<DatabaseFormula> dbfs;
	private final Set<Query> queries;
	/**
	 * Output
	 */
	private final Set<String> undefined = new HashSet<>();
	private final Set<String> unused = new HashSet<>();

	/**
	 * Creates an analyzer.
	 */
	public Analyzer(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		this.dbfs = dbfs;
		this.queries = queries;
	}

	public void analyze() {
		for (DatabaseFormula dbf : this.dbfs) {
			addDefinition(dbf);
		}
		for (Query query : this.queries) {
			addQuery(query);
		}
		this.undefined.addAll(this.used.keySet());
		this.undefined.removeAll(this.definitions.keySet());
		this.unused.addAll(this.definitions.keySet());
		this.unused.removeAll(this.used.keySet());
	}

	public Set<Query> getUndefined() {
		Set<Query> undefined = new HashSet<>();
		for (String undf : this.undefined) {
			if (!undf.equals("me/1")) {
				undefined.addAll(this.used.get(undf));
			}
		}
		return undefined;
	}

	public Set<DatabaseFormula> getUnused() {
		Set<DatabaseFormula> unused = new HashSet<>();
		for (String df : this.unused) {
			unused.addAll(this.definitions.get(df));
		}
		return unused;
	}

	/**
	 * Assumes the given DatabaseFormula is either a single term, or the :-/2
	 * function.
	 */
	private void addDefinition(DatabaseFormula formula) {
		PrologDBFormula plFormula = (PrologDBFormula) formula;
		alice.tuprolog.Term term = plFormula.getTerm();
		alice.tuprolog.Term headTerm = term;
		// The :- function needs special attention.
		if (term instanceof alice.tuprolog.Struct) {
			alice.tuprolog.Struct struct = (alice.tuprolog.Struct) term;
			if (struct.getName().equals(":-") && struct.getArity() == 1) {
				// Directive: the first argument is a query.
				addQuery(new PrologQuery(struct.getArg(0), formula.getSourceInfo()));
			} else if (struct.getName().equals(":-") && struct.getArity() == 2) {
				// The first argument is the only defined term.
				headTerm = struct.getArg(0);
				// The other argument is a conjunction of queried terms.
				addQuery(new PrologQuery(struct.getArg(1), formula.getSourceInfo()));
			}
		}
		if (headTerm instanceof alice.tuprolog.Struct) {
			alice.tuprolog.Struct headStruct = (alice.tuprolog.Struct) headTerm;
			String headSig = headStruct.getName() + "/" + headStruct.getArity();
			if (headSig.equals("//2")) {
				// the term is already a signature itself
				headSig = headStruct.getArg(0) + "/" + headStruct.getArg(1);
			}
			// Ignore built-in operators.
			if (!PrologOperators.prologBuiltin(headSig)) {
				// Add a new definition node
				List<PrologDBFormula> formulas;
				if (this.definitions.containsKey(headSig)) {
					formulas = this.definitions.get(headSig);
				} else {
					formulas = new LinkedList<>();
				}
				formulas.add(new PrologDBFormula(headTerm, formula.getSourceInfo()));
				this.definitions.put(headSig, formulas);
			}
		}
	}

	/**
	 * Add a query (a use of predicates).
	 */
	public void addQuery(Query query) {
		addQuery(((PrologQuery) query).getTerm(), query.getSourceInfo());
	}

	/**
	 * Add predicates used in a clause as queries.
	 */
	public void addQuery(DatabaseFormula formula) {
		// we may assume the formula is a single term, so we can just
		// as well handle the inner term as a general term.
		addQuery(((PrologDBFormula) formula).getTerm(), formula.getSourceInfo());
	}

	private void addQuery(alice.tuprolog.Term plTerm, SourceInfo info) {
		// check if the term needs to be unpacked
		String termSig = (plTerm instanceof alice.tuprolog.Struct)
				? (((alice.tuprolog.Struct) plTerm).getName() + "/" + ((alice.tuprolog.Struct) plTerm).getArity()) : "";
		// there is only one /1 operator we need to unpack: not/1
		if (termSig.equals("not/1")) {
			addQuery(((alice.tuprolog.Struct) plTerm).getArg(0), info);
		} else if (termSig.equals(";/2") || termSig.equals(",/2") || termSig.equals("forall/2")) {
			// unpack the conjunction, disjunction and forall /2-operators
			addQuery(((alice.tuprolog.Struct) plTerm).getArg(0), info);
			addQuery(((alice.tuprolog.Struct) plTerm).getArg(1), info);
		} else if (termSig.equals("findall/3") || termSig.equals("setof/3") || termSig.equals("aggregate/3")
				|| termSig.equals("aggregate_all/3")) {
			// findall, setof aggregate and aggregate_all /3-operators only
			// have a query in the second argument.
			addQuery(((alice.tuprolog.Struct) plTerm).getArg(1), info);
		} else if (termSig.equals("aggregate/4") || termSig.equals("aggregate_all/4")) {
			// aggregate and aggregate_all /4-operators have the query in
			// the third argument.
			addQuery(((alice.tuprolog.Struct) plTerm).getArg(2), info);
		} else if (termSig.equals("predsort/3")) {
			// first argument is name that will be called as name/3
			alice.tuprolog.Term stubarg = ((alice.tuprolog.Struct) plTerm).getArg(0);
			alice.tuprolog.Term stubfunc = new alice.tuprolog.Struct(((alice.tuprolog.Struct) stubarg).getName(),
					new alice.tuprolog.Term[] { ANON_VAR, ANON_VAR, ANON_VAR });
			addQuery(stubfunc, info);
		} else if (termSig.equals("dynamic/1")) {
			// recognize predicate declarations
			addDefinition(new PrologDBFormula(((alice.tuprolog.Struct) plTerm).getArg(0), info));
		} else if (!PrologOperators.prologBuiltin(termSig)) {
			// if we get here, the term should not be unpacked
			// but needs to be added as a query node
			// Add a new definition node
			List<PrologQuery> formulas;
			if (this.used.containsKey(termSig)) {
				formulas = this.used.get(termSig);
			} else {
				formulas = new LinkedList<>();
			}
			formulas.add(new PrologQuery(plTerm, info));
			this.used.put(termSig, formulas);
		}
	}
}
