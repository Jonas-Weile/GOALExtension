package owlrepo.language;

import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLUnaryAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

public class SWRLTerm extends SWRLExpression implements Term {

	SWRLAtom atom;
	SWRLArgument argument;

	public SWRLTerm(SWRLRule rule) {
		super(rule);
		Set<SWRLAtom> body = super.getRule().getBody();
		// assume there's only one atom in the body
		this.atom = body.iterator().next();

	}

	public SWRLTerm(SWRLAtom atom) {
		super(atom);
		this.atom = atom;
		this.argument = null;

	}

	public SWRLTerm(SWRLArgument arg) {
		super(arg);
		this.argument = arg;
		this.atom = null;
	}

	public SWRLTerm(String string) {
		super(string);
	}

	public SWRLAtom getAtom() {
		return this.atom;
	}

	public SWRLArgument getArgument() {
		return this.argument;
	}

	public SWRLVar getVariable() {
		if (this.isVariable())
			return new SWRLVar((SWRLVariable) this.argument);
		return null;
	}

	public boolean isArgument() {
		return (this.argument != null);
	}

	public boolean isVariable() {
		return (this.argument instanceof SWRLVariable);
	}

	public boolean isLiteral() {
		return (this.argument instanceof SWRLLiteralArgument);
	}

	public boolean isIndividual() {
		return (this.argument instanceof SWRLIndividualArgument);
	}

	public boolean isAtom() {
		return (this.atom != null);
	}

	public boolean isClassAtom() {
		return (this.atom instanceof SWRLUnaryAtom);
	}

	public boolean isDataAtom() {
		return (this.atom instanceof SWRLDataPropertyAtom);
	}

	public boolean isObjectAtom() {
		return (this.atom instanceof SWRLObjectPropertyAtom);
	}

	public boolean isDiffIndividualsAtom() {
		return (this.atom instanceof SWRLDifferentIndividualsAtom);
	}

	public boolean isSameIndividualAtom() {
		return (this.atom instanceof SWRLSameIndividualAtom);
	}

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables
	 * that are bound to a term in the substitution by that term (or, only
	 * renames in case the substitution binds a variable to another one).
	 */
	public Term applySubst(Substitution substitution) {
		return (Term) super.applySubst(substitution);
		// SWRLSubstitution subst = (SWRLSubstitution)substitution;
		// if (this.isVariable() && subst.getVariables().contains(this.argument)
		// ){
		// return new
		// SWRLTerm(subst.getSWRLArgument((SWRLVariable)this.argument));
		// }
		// else if (this.isTerm()){
		// for (SWRLArgument arg: this.atom.getAllArguments()){
		// SWRLExpression exp = new SWRLExpression(arg);
		//
		//
		// }
		// }
		// return this;
	}

	@Override
	public String toString() {
		if (rule != null)
			return rule.toString();
		if (atom != null)
			return atom.toString();
		if (argument != null)
			return argument.toString();
		return this.toString();
	}
}
