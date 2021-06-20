package jasonkri;

import jason.asSemantics.Unifier;
import jason.asSyntax.BinaryStructure;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogExpr.LogicalOp;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.RelExpr;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.language.Query;
import krTools.language.Update;

/**
 * Utility functions
 * 
 * @author W.Pasman 9jun15
 *
 */
public class Utils {

	/**
	 * get the conjunction elements from {@link LogExpr}. If it's just 1 term or
	 * not a LogExpr, it's returned as is. Order of terms is preserved. Both
	 * left and right hand terms can recurse into more {@link LogExpr}s.
	 * 
	 * @param conjunct
	 *            a LogExpr. We ask for {@link LogExpr} and not for
	 *            {@link LogicalFormula} because LogExpr also extends
	 *            {@link Structure}.
	 * @return unzipped elements in the conjunct. NOTICE there may be
	 *         {@link RelExpr} end up in the returned terms. Depending on your
	 *         needs , you may want to check the result further.
	 */
	public static List<LogicalFormula> getConjuncts(Term formula) {
		List<LogicalFormula> terms = new ArrayList<LogicalFormula>();

		if (!(formula instanceof LogExpr)) {
			terms.add((LogicalFormula) formula);
			return terms;
		}

		// if we get here, formula is a LogExpr
		LogExpr conjunct = (LogExpr) formula;

		switch (conjunct.getOp()) {
		case and:
			terms.addAll(getConjuncts(conjunct.getLHS()));
			terms.addAll(getConjuncts(conjunct.getRHS()));
			break;
		case none: // why is this a LogExpr anyway?
		case or: // is this acceptable?
		case not:
			terms.add(conjunct);
			break;
		}
		return terms;
	}

	/**
	 * Make a logical conjunct out of a list of terms, using right side binding.
	 * If there is only 1 term in the list, returns the one term.
	 * 
	 * @param terms
	 *            list of {@link Term}s
	 * @return conjunct
	 */
	public static LogicalFormula makeConjunct(List<LogicalFormula> terms) {
		if (terms.size() == 0) {
			throw new IllegalArgumentException(
					"can not make conjunct without terms");
		}
		if (terms.size() == 1) {
			return terms.get(0);
		}
		return new LogExpr(terms.get(0), LogicalOp.and,
				makeConjunct(terms.subList(1, terms.size())));
	}

	/**
	 * Check if given term contains a {@link BinaryStructure}.
	 * 
	 * @param term
	 *            term to check.
	 * @return true iff term contains a {@link RelExpr}.
	 */
	public static boolean containsBinaryStructure(Term term) {
		if (term instanceof BinaryStructure) {
			return true;
		}
		if (term instanceof ListTerm) {
			if (((ListTerm) term).getAsList() == null) {
				// workaround for bug in getAsList, iterator etc. #3579, #3580
				return false;
			}
			for (Term t : (ListTerm) term) {
				if (containsBinaryStructure(t))
					return true;
			}
			return false;
		}
		if (term instanceof Literal) {
			for (Term t : ((Literal) term).getTerms()) {
				if (containsBinaryStructure(t))
					return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Check if one of the terms contains a {@link RelExpr}
	 * 
	 * @param terms
	 * @return true iff at least one term contains a {@link RelExpr}
	 */
	public static boolean containsBinaryStructure(List<? extends Term> terms) {
		for (Term t : terms) {
			if (containsBinaryStructure(t)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if term is a predicate or of form not(predicate).
	 * 
	 * @param t
	 * @return
	 */
	public static boolean isPosOrNegPredicate(Term t) {
		if (isNegation(t)) {
			return !containsBinaryStructure(((LogExpr) t).getLHS());
		}
		return !containsBinaryStructure(t);
	}

	/**
	 * Check if term is negation of the form not(Term).
	 * 
	 * @param t
	 *            term to check
	 * @return true iff term is negation.
	 */
	public static boolean isNegation(Term t) {
		return t instanceof LogExpr && ((LogExpr) t).getOp() == LogicalOp.not;
	}

	/**
	 * Check if this expression is a good database formula.
	 * 
	 * @return true iff expression is a good database formula. It's a good
	 *         database formula if it's a Structure but not a BinaryStructure.
	 */
	public static boolean isDatabaseFormula(Term term) {
		return term instanceof Structure && !(term instanceof BinaryStructure);
	}

	/**
	 * @return {@code true} iff this expression can be used as a {@link Query}
	 *         (after conversion using {@link #toQuery()}).
	 */
	public static boolean isQuery(Term term) {
		return term instanceof LogicalFormula && !term.isRule();
	}

	/**
	 * Check if term is usable as {@link Update}. An update is a conjunct of
	 * singleUpdates
	 * 
	 * @return true iff term is usable as {@link Update}. Notice that we also
	 *         accept "true" as special update term (it's a Literal.TrueLiteral
	 *         which is not even publicly visible.).
	 */
	public static boolean isUpdate(Term term) {
		if (term instanceof LogicalFormula) {
			// test all elements. Top level can be conjunct
			List<LogicalFormula> terms = Utils.getConjuncts(term);

			for (Term t : terms) {
				if (!isSingleUpdate(t)) {
					return false;
				}
			}
			return true;
		}

		// the other cases - ObjectTerm, StringTerm, ListTerm, FalseLiteral,
		// TrueLiteralare not good updates.
		return false;
	}

	public static boolean isSingleUpdate(Term term) {
		// special case: "true".
		if (isTrueLiteral(term)) {
			return true;
		}
		if (isNegation(term)) {
			return isDatabaseFormula(((LogExpr) term).getLHS());
		}
		return isDatabaseFormula(term);
	}

	/**
	 * @param term
	 * @return true iff the term is a TrueLiteral.
	 */
	public static boolean isTrueLiteral(Term term) {
		return term instanceof Literal
				&& ((Literal) term).getFunctor().equals("true");
	}

	/**
	 * Create most general unifier (mgu) of two terms. The returned most general
	 * unifier is a substitution which, if applied to both terms, will make the
	 * terms equal. This algorithm currently will first try to set variables in
	 * x. So it has a bias towards filling in the x variables over filling in y
	 * variables. <br>
	 * The map that is returned contains String as key objects, because
	 * jpl.Variable can not be used for key (as it does not implement hashCode).
	 * 
	 * @param x
	 *            first term
	 * @param y
	 *            second term
	 * @return mgu, or null if no mgu exists.
	 */
	public static Map<String, Term> mgu(Term jasonTerm, Term otherterm) {
		return unify(jasonTerm, otherterm, new HashMap<String, Term>());
	}

	/**
	 * Textbook implementation of Unify algorithm. AI : A modern Approach,
	 * Russel, Norvig, Third Edition unifies two terms and returns set of
	 * substitutions that make the terms unify. The variables in the two terms
	 * are assumed to be in the same namespace. <br>
	 * This textbook implementation has a bias towards assigning variables in
	 * the left hand term. <br>
	 * The returned map contains String as key objects, because jpl.Variable can
	 * not be used for key (as it does not implement hashCode).
	 * 
	 * @param x
	 *            the first term.
	 * @param y
	 *            the second term.
	 * @param s
	 *            the substitutions used so far.
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */
	public static Map<String, Term> unify(Term x, Term y, Map<String, Term> s) {
		if (s == null) {
			return null;
		}
		if (x.equals(y)) {
			return s;
		}
		if (x.isVar()) {
			return unifyVar((VarTerm) x, y, s);
		}
		if (y.isVar()) {
			return unifyVar((VarTerm) y, x, s);
		}
		if (x instanceof Literal && y instanceof Literal) {
			return unifyCompounds((Literal) x, (Literal) y, s);
		}
		// we do not have lists. List is just another compound.
		return null;
	}

	/**
	 * Unify 2 {@link Compound}s. Implements the bit vague element in the
	 * textbook UNIFY(x.ARGS, y.ARGS, UNIFY(x.OP, y.OP,s)).
	 *
	 * @param x
	 *            the first {@link Compound}
	 * @param y
	 *            The second {@link Compound}
	 * @param s
	 *            the substitutions used so far. s must not be null.
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */
	private static Map<String, Term> unifyCompounds(Literal x, Literal y,
			Map<String, Term> s) {
		if (x.getArity() != y.getArity()) {
			return null;
		}
		if (!x.getFunctor().equals(y.getFunctor())) {
			return null;
		}
		for (int i = 1; i <= x.getArity(); i++) {
			s = unify(x.getTerm(i), y.getTerm(i), s);
		}
		return s;
	}

	/**
	 * Textbook implementation of Unify-Var algorithm. AI : A modern Approach,
	 * Russel, Norvig, Third Edition. unifies two terms and returns set of
	 * substitutions that make the terms unify. The variables in the two terms
	 * are assumed to be in the same namespace.
	 *
	 * @param var
	 *            the {@link jpl.Variable}.
	 * @param y
	 *            the {@link Term}.
	 * @param s
	 *            the substitutions used so far. Must not be null. This set can
	 *            be modified by this function.
	 *
	 * @return set of variable substitutions, or null if the terms do not unify.
	 */

	private static Map<String, Term> unifyVar(VarTerm var, Term x,
			Map<String, Term> s) {
		if (s.containsKey(var.getFunctor())) {
			return unify(s.get(var.getFunctor()), x, s);
		}
		if (x.isVar() && s.containsKey(((VarTerm) x).getFunctor())) {
			return unify(var, s.get(((VarTerm) x).getFunctor()), s);
		}
		if (getFreeVar(x).contains(var)) {
			return null;
		}
		s.put(var.getFunctor(), x);
		return s;
	}

	public static Set<VarTerm> getFreeVar(Term term) {
		Set<VarTerm> vars = new HashSet<VarTerm>();
		if (term.isVar()) {
			vars.add((VarTerm) term);

		} else if (term.isLiteral() || term.isList()) {
			for (VarTerm var : ((Literal) term).getSingletonVars()) {
				// HACK we lost the source info of subterms. #3554
				vars.add(var);
			}

		}
		return vars;
	}

	/**
	 * Convert map of <varname, term> to a {@link Unifier}
	 * 
	 * @param map
	 * @return {@link Unifier}
	 */
	public static Unifier convertMapToUnifier(Map<String, Term> map) {
		Unifier unifier = new Unifier();
		for (String varname : map.keySet()) {
			unifier.bind(new VarTerm(varname), map.get(varname));
		}
		return unifier;
	}

	/**
	 * Create a compound term with given operator and terms
	 * 
	 * @param operator
	 *            the name of the predicate
	 * @param args
	 *            the arguments of the predicate.
	 * @return
	 */
	public static LiteralImpl createPred(String operator, Term... args) {
		LiteralImpl pred = new LiteralImpl(operator);
		for (Term t : args) {
			pred.addTerm(t);
		}
		return pred;
	}

	/**
	 * create jason List from a set of Terms
	 * 
	 * @param termList
	 * @return
	 */
	public static ListTerm makeList(List<Term> termList) {
		ListTermImpl list = new ListTermImpl();
		for (Term term : termList) {
			list.add(term);
		}
		return list;
	}

}