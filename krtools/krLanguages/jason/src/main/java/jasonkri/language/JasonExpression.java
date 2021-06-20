package jasonkri.language;

import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.Rule;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import jasonkri.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

/**
 * A JasonExpression is a basic placeholder for JASON {@link Term}s. It collects
 * functionality that is shared with many derived classes and therefore is very
 * generic. It only does not allow {@link Plan} related objects.
 *
 */
public class JasonExpression implements Expression {

	/**
	 * A Jason term (structure).
	 */
	private final Term term;

	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * JasonExpressions are equal iff the {@link Term}s are equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JasonExpression)) {
			return false;
		}

		return term.equals(((JasonExpression) o).getJasonTerm());
	}

	/**
	 * Information about the source used to construct this expression. #3582
	 * this needs to be a general SourceInfo field, as we may inherit
	 * {@link SourceInfo} from other objects eg messages.
	 */
	private final SourceInfo info;

	/**
	 * Creates a new term from a Jason structure (term).
	 * 
	 * @param s
	 *            A Jason {@link Structure}.
	 * @param i
	 *            {@link SourceInfo}. This must be general source info as we can
	 *            inherit {@link SourceInfo} from other objects, e.g. messages
	 *            and eis objects.
	 */
	public JasonExpression(Term s, SourceInfo i) {
		// this can never be a PlanBody because PlanBody does not extend
		// Structure.
		if (s instanceof Plan) {
			throw new IllegalArgumentException("structure " + s
					+ " is a plan, which is not supported"); // it's a bug
		}

		term = s;
		info = i;
	}

	/**
	 * @return The original Jason {@link Structure}
	 */
	public Term getJasonTerm() {
		return this.term;
	}

	@Override
	public String getSignature() {
		if (term instanceof Literal) {
			/**
			 */
			Literal literal = (Literal) term;
			return literal.getFunctor() + "/" + literal.getArity();
		}
		// strings, LogicalFormula do not have signature in Jason.
		return null;
	}

	/**
	 * Get the name of this term, this is the top functor of the term
	 * 
	 * @return to functor of the term. Or null if term does not have name in
	 *         Jason.
	 * 
	 */
	public String getName() {
		if (term instanceof Literal) {
			/**
			 * note, {@link ListTerm} does not have getFunctor but ListTermImpl
			 * does and that is what we really have for Lists.
			 */
			return ((Literal) term).getFunctor();
		}
		// strings, LogicalFormula do not have signature in Jason.
		return null;
	}

	@Override
	public boolean isVar() {
		return false;
	}

	@Override
	public boolean isClosed() {
		if (isRule()) {
			// logical formula in JASON is weird, it only checks the head.
			return term.isGround() && ((Rule) term).getBody().isGround();
		}
		return this.term.isGround();
	}

	@Override
	public Set<Var> getFreeVar() { // FIXME add unit tests
		// CHECK can we use utils.getFreeVar?
		Set<Var> vars = new HashSet<Var>();
		if (term.isVar()) {
			vars.add(new JasonVar((VarTerm) term, info));

		} else if (term.isLiteral() || term.isList()) {
			Map<VarTerm, Integer> count = new HashMap<VarTerm, Integer>();
			// HACK we lost the source info of subterms. #3554
			((Literal) term).countVars(count);
			for (VarTerm var : count.keySet()) {
				vars.add(new JasonVar(var, info));
			}

		}
		return vars;
	}

	@Override
	public Substitution mgu(Expression expression) {
		Term otherterm = ((JasonExpression) expression).getJasonTerm();
		Map<String, Term> map = Utils.mgu(this.getJasonTerm(), otherterm);
		if (map == null) {
			return null; // no solution.
		}
		return new JasonSubstitution(Utils.convertMapToUnifier(map));
	}

	@Override
	public SourceInfo getSourceInfo() {
		return info;
	}

	@Override
	public Expression applySubst(Substitution substitution) {
		return new JasonExpression(substitute(substitution), info);
	}

	/**
	 * Support for applySubst in subclasses
	 * 
	 * @param substitution
	 * @return {@link Structure}
	 */
	public Term substitute(Substitution substitution) {
		return term.capply(((JasonSubstitution) substitution).getUnifier());
	}

	public boolean isArithExpr() {
		return term.isArithExpr();
	}

	public boolean isAtom() {
		return term.isAtom();
	}

	public boolean isInternalAction() {
		return term.isInternalAction();
	}

	public boolean isList() {
		return term.isList();
	}

	public boolean isNumeric() {
		return term.isNumeric();
	}

	public boolean isRule() {
		return term.isRule();
	}

	@Override
	public String toString() {
		return term.toString();
	}

	public boolean isQuery() {
		return Utils.isQuery(term);
	}

	public boolean isUpdate() {
		return Utils.isUpdate(term);
	}

}
