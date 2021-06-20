/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package swiprolog.dependency;

import java.util.LinkedList;
import java.util.List;

import krTools.dependency.DependencyGraph;
import krTools.dependency.Node;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;
import swiprolog.parser.PrologOperators;

/**
 * A dependency graph for the SWI Prolog language.
 */
public class PrologDependencyGraph extends DependencyGraph<Term> {
	/**
	 * {@inheritDoc} <br>
	 *
	 * Assumes the given {@link DatabaseFormula} is a {@link PrologDBFormula}, i.e.
	 * either a simple fact, or a clause of the form p(...):-(...) using the
	 * operator :-/2.
	 *
	 * @throws GOALUserError
	 */
	@Override
	public void add(DatabaseFormula formula, boolean defined, boolean queried) throws KRException {
		PrologCompound term = ((PrologDBFormula) formula).getCompound();
		/**
		 * The :- function needs to be treated differently from other terms; the head
		 * term is defined, whereas the terms in the body are queried.
		 */
		if (term.isQuery()) {
			if (defined) {
				// The first argument is the term that is being defined.
				Term content1 = term.getArg(0);
				List<Node<Term>> definitionNode = addTerm(content1, true, false);
				// The other argument consists of terms that are queried.
				Term content2 = term.getArg(1);
				List<Node<Term>> queryNodes = addTerm(content2, false, true);
				for (Node<Term> node : queryNodes) {
					definitionNode.get(0).addDependency(node);
				}
			}
			if (queried) {
				throw new KRDatabaseException("a clause with main operator :-/2 cannot be queried.");
			}
		} else {
			String signature = term.getSignature();
			if (reserved(signature) && defined) {
				throw new KRDatabaseException("illegal attempt to redefine '" + signature + "'.");
			} else {
				addTerm(term, defined, queried);
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void add(Query query) throws KRException {
		PrologCompound compound = ((PrologQuery) query).getCompound();
		if (compound.isQuery()) {
			throw new KRDatabaseException("a clause with main operator :-/2 cannot be queried.");
		} else {
			addTerm(compound, false, true);
		}
	}

	/**
	 * Creates nodes for terms, if not already present. Note that the ":-/2"
	 * operator is taken care of by {@link #add(DatabaseFormula, boolean, boolean)}.
	 *
	 * @param prologTerm
	 * @param defined
	 * @param queried
	 * @return The list of nodes associated with the term (either created or already
	 *         existing nodes).
	 */
	private List<Node<Term>> addTerm(Term prologTerm, boolean defined, boolean queried) {
		List<Node<Term>> nodes = new LinkedList<>();
		// Unpack the term if needed (if so, we're handling a query).
		for (Term term : unpack(prologTerm)) {
			String signature = term.getSignature();
			// Ignore built-in operators of Prolog as well as reserved GOAL
			// operators.
			if (!reserved(signature)) {
				Node<Term> node = super.graph.get(signature);
				if (node == null) {
					node = new Node<>(signature);
					super.graph.put(signature, node);
				}
				if (defined) {
					node.addDefinition(term);
				}
				if (queried) {
					node.addQuery(term);
				}
				nodes.add(node);
			}
		}
		return nodes;
	}

	/**
	 * Unpacks the given {@link Term} and returns all simple facts that do not have
	 * any occurrences of built-in Prolog operators or reserved GOAL operators.
	 * <p>
	 * Unpacking is needed if the term contains at top level a build-in predicate
	 * and that built-in predicate will cause further querying in the SWI engine.
	 * For example not(pred) will cause invocation of pred.
	 *
	 * @param term
	 *            The term that is unpacked.
	 * @return The resulting terms without any built-in or reserved operators. May
	 *         be the empty list.
	 */
	private List<Term> unpack(Term term) {
		List<Term> terms = new LinkedList<>();
		String signature = term.getSignature();
		// If we need to unpack the operators below, we're dealing with a query.
		if (signature.equals("not/1")) {
			PrologCompound content = (PrologCompound) ((PrologCompound) term).getArg(0);
			terms.addAll(unpack(content));
		} else if (signature.equals("include/3")) {
			/*
			 * special case. first argument of include/3 is the NAME of the func but without
			 * the required argument. We have to make up the correct term.
			 */
			// CHECK we assume here that arg is plain atom. What if not??
			PrologCompound content = (PrologCompound) ((PrologCompound) term).getArg(0);
			Term anon = PrologImplFactory.getVar("_", null);
			Term stubfunc = PrologImplFactory.getCompound(content.getName(), new Term[] { anon }, term.getSourceInfo());
			terms.add(stubfunc);
		} else if (signature.equals(";/2") || signature.equals(",/2") || signature.equals("forall/2")) {
			// Unpack the conjunction, disjunction and forall /2-operators.
			PrologCompound content = (PrologCompound) term;
			for (Term arg : content) {
				terms.addAll(unpack(arg));
			}
		} else if (signature.equals("findall/3") || signature.equals("setof/3") || signature.equals("aggregate/3")
				|| signature.equals("aggregate_all/3")) {
			// findall, setof aggregate and aggregate_all /3-operators only
			// have a query in the second argument.
			PrologCompound content = (PrologCompound) ((PrologCompound) term).getArg(1);
			terms.addAll(unpack(content));
		} else if (signature.equals("aggregate/4") || signature.equals("aggregate_all/4")) {
			// aggregate and aggregate_all /4-operators have the query in
			// the third argument.
			PrologCompound content = (PrologCompound) ((PrologCompound) term).getArg(2);
			terms.addAll(unpack(content));
		} else if (signature.equals("predsort/3")) {
			/*
			 * special case. first argument of predsort is the NAME of the func but without
			 * the required 3 arguments. We have to make up the correct term. We will be
			 * using 3 anonymous variables.
			 */
			// CHECK we assume here that arg is plain atom. What if not??
			PrologCompound content = (PrologCompound) ((PrologCompound) term).getArg(0);
			Term anon = PrologImplFactory.getVar("_", null);
			Term stubfunc = PrologImplFactory.getCompound(content.getName(), new Term[] { anon, anon, anon },
					term.getSourceInfo());
			terms.add(stubfunc);
		} else {
			terms.add(term);
		}
		return terms;
	}

	/**
	 * Indicates whether the given signature represents a reserved (Prolog or GOAL)
	 * operator.
	 *
	 * @param signature
	 * @return {@code true} if the signature represents a reserved operator.
	 */
	private boolean reserved(String signature) {
		return PrologOperators.prologBuiltin(signature);
	}
}
