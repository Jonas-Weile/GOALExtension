package swiprolog.language;

import java.util.List;

import krTools.language.Substitution;
import krTools.language.Term;

public interface PrologCompound extends PrologTerm, Iterable<Term> {
	/**
	 * @return the (unquoted) name of this Compound
	 */
	public String getName();

	/**
	 * @return the arity (1+) of this Compound
	 */
	public int getArity();

	/**
	 * @return the ith argument (counting from 0) of this Compound
	 */
	public Term getArg(int i);

	/**
	 * @return true iff D-is-a-predication in ISO p.132-.
	 */
	public boolean isPredication();

	/**
	 * @return true iff the term is a predicate indicator as with ISO 3.90: a term
	 *         of form '/'(Atom, Integer).
	 */
	public boolean isPredicateIndicator();

	/**
	 * @return true iff the term is a directive: a term of form ':-' Term.
	 */
	public boolean isDirective();

	/**
	 * Returns the operands of a (repeatedly used) right associative binary
	 * operator.
	 * <p>
	 * Can be used, for example, to get the conjuncts of a conjunction or the
	 * elements of a list. Note that the <i>second</i> conjunct or element in a list
	 * concatenation can be a conjunct or list itself again.
	 * </p>
	 * <p>
	 * A list (term) of the form '.'(a,'.'(b,'.'(c, []))), for example, returns the
	 * elements a, b, c, <i>and</i> the empty list []. A conjunction of the form
	 * ','(e0,','(e1,','(e2...((...,en)))...) returns the list of conjuncts e0, e1,
	 * e2, etc.
	 * </p>
	 *
	 * @param operator
	 *            The binary operator.
	 * @return A list of operands.
	 */
	public List<Term> getOperands(String operator);

	@Override
	public default boolean isNumeric() {
		return false;
	}

	@Override
	public default Substitution unify(Term x, Substitution s) {
		if (s == null) {
			return null;
		} else if (equals(x)) {
			return s;
		} else if (x instanceof PrologCompound) {
			PrologCompound y = (PrologCompound) x;
			if ((getArity() == y.getArity()) && getName().equals(y.getName())) {
				for (int i = 0; i < getArity(); ++i) {
					s = ((PrologTerm) getArg(i)).unify(y.getArg(i), s);
				}
				return s;
			} else {
				return null;
			}
		} else if (x instanceof PrologVar) {
			PrologVar var = (PrologVar) x;
			return var.unify(this, s);
		} else {
			return null;
		}
	}
}
