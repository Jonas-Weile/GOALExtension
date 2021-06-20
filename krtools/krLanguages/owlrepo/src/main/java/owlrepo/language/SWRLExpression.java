package owlrepo.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import owlrepo.parser.SWRLParserSourceInfo;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class SWRLExpression implements Expression {

	OWLObject expression; // when to be used?

	// possible values:
	OWLAxiom axiom;
	SWRLRule rule;
	SWRLAtom atom, namedGraphAtom;
	SWRLArgument argument;
	int type = -1;

	OWLDataFactory df = new OWLDataFactoryImpl();

	public SWRLExpression(OWLAxiom axiom) {
		this.axiom = axiom;
		this.expression = axiom;
		this.type = 0;
		// since we get OWLAxiom, we need to construct SWRL constructs
		if (axiom instanceof OWLIndividualAxiom) { // atom - term
			SWRLAtom atom = null;
			if (axiom instanceof OWLClassAssertionAxiom){
				OWLClassAssertionAxiom classAxiom = (OWLClassAssertionAxiom)axiom;
				atom = df.getSWRLClassAtom(classAxiom.getClassExpression(), df
						.getSWRLIndividualArgument(classAxiom.getIndividual()));
			} else if (axiom instanceof OWLDataPropertyAssertionAxiom){
				OWLDataPropertyAssertionAxiom dpropAxiom = (OWLDataPropertyAssertionAxiom) axiom;
				atom = df.getSWRLDataPropertyAtom(dpropAxiom.getProperty(),
						df.getSWRLIndividualArgument(dpropAxiom.getSubject()),
						df.getSWRLLiteralArgument(dpropAxiom.getObject()));
			} else if (axiom instanceof OWLObjectPropertyAssertionAxiom){
				OWLObjectPropertyAssertionAxiom opropAxiom = (OWLObjectPropertyAssertionAxiom) axiom;
				atom = df.getSWRLObjectPropertyAtom(opropAxiom.getProperty(),
						df.getSWRLIndividualArgument(opropAxiom.getSubject()),
						df.getSWRLIndividualArgument(opropAxiom.getObject()));
			}
			if (atom != null) {
				// this(atom);
				Set<SWRLAtom> atoms = new HashSet<SWRLAtom>();
				atoms.add(atom);
				this.rule = df.getSWRLRule(atoms, new HashSet<SWRLAtom>()); // body,head
				this.axiom = rule;
				this.atom = atom;
				this.expression = atom;
				this.type = 2;
			}
			else {
				//something went wrong
				System.out.println("Could not construct term from axiom "+axiom);
			}
		} else if (axiom instanceof SWRLRule) { // in case it's a rule
			this.rule = (SWRLRule) axiom;
			this.axiom = rule;
			this.expression = rule;
			this.type = 1;
		}
	}

	public SWRLExpression(SWRLRule rule) {
		this.rule = rule;
		this.axiom = rule;
		this.expression = rule;
		this.type = 1;
	}

	public SWRLExpression(SWRLAtom atom) {
		Set<SWRLAtom> atoms = new HashSet<SWRLAtom>();
		Set<SWRLAtom> head = atoms;
		atoms.add(atom);
		this.rule = df.getSWRLRule(atoms, new HashSet<SWRLAtom>()); // body,head
		this.axiom = rule;
		this.atom = atom;
		this.expression = atom;
		this.type = 2;
	}

	public SWRLExpression(SWRLArgument arg) {
		this.argument = arg;
		this.expression = arg;
		this.type = 3;
	}

	/**
	 * used only for creating a query out of an undefined term string that the
	 * parser could not parse (not in ontology) used only by
	 * OWLRepoKRInterface.getUndefined
	 * 
	 * @param string
	 */
	public SWRLExpression(String string) {
		this.argument = df.getSWRLLiteralArgument(df.getOWLLiteral(string));
		this.expression = argument;
		this.type = -1; // error
	}

	public String getSignature() {
		// System.out.println("get signature " + expression.toString());
		if (this.isArgument())
			return argument.getSignature().toString();
		else if (this.isTerm())
			return atom.getSignature().toString();
		else if (this.isRule())
			return rule.getSignature().toString();
		else
			return expression.toString().substring(0,
					expression.toString().indexOf("^^"));
	}

	public SWRLRule getRule() {
		return this.rule;
	}

	public OWLAxiom getAxiom() {
		return this.axiom;
	}

	public SWRLAtom getAtom() {
		return this.atom;
	}

	public SWRLArgument getArgument() {
		return this.argument;
	}

	public boolean isUndefined() {
		return (this.type == -1);
	}

	public boolean isTerm() {
		return (this.type == 2);
	}

	public boolean isArgument() {
		return (this.type == 3);
	}

	public boolean isVar() {
		return (this.argument != null && this.argument instanceof SWRLVariable);
	}

	public boolean isRule() {
		return (this.type == 1);
	}

	public boolean isIndividual() {
		if (this.isArgument() && !this.isVar())
			if (this.argument instanceof SWRLIArgument)
				return true;
		return false;
	}

	public boolean isConstant() {
		if (this.isArgument() && !this.isVar())
			if (this.argument instanceof SWRLDArgument)
				return true;
		return false;
	}

	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}


	public SWRLAtom createNamedGraphAtom(String id){
		this.namedGraphAtom = df.getSWRLClassAtom(df.getOWLThing(),
 df
				.getSWRLIndividualArgument(df.getOWLNamedIndividual(IRI
						.create("http://ii.tudelft.nl/goal#" + id))));
		return this.namedGraphAtom;
	}

	public SWRLAtom getNamedGraphAtom(){
		if (this.namedGraphAtom != null)
			return this.namedGraphAtom;
		System.out.println("Named graph atom was not added to: "+this);
		return null;
	}

	public Set<Var> getFreeVar() {
		HashSet<Var> vars = new HashSet<Var>();
		if (this.isArgument()) {
			if (this.isVar()) {// variable
				vars.add(new SWRLVar((SWRLVariable) this.argument));
			} else
				return vars;
		} else if (this.isTerm()) {// atom
			for (SWRLArgument arg : this.atom.getAllArguments()) {
				Var fvar = getFreeVar(arg);
				if (fvar != null)
					vars.add(fvar);
			}
		} else if (this.isRule()) { // rule
			for (SWRLVariable v : rule.getVariables()) {
				vars.add(new SWRLVar(v));
			}
		}
		return vars;
	}

	private Var getFreeVar(SWRLArgument arg) {
		if (arg instanceof SWRLVariable)
			return new SWRLVar(((SWRLVariable) arg));
		return null;
	}

	@Override
	public Expression applySubst(Substitution substitution) {
		SWRLSubstitution subst = null;
		// System.out.println("Apply subst: " + substitution.toString() + " to +
		// this.toString());
		if (substitution instanceof SWRLSubstitution)
			subst = (SWRLSubstitution) substitution;
		// if it's a swrl substitution
		if (subst != null) {
			// if substitution and current expression have both variables
			if (!subst.getSWRLVariables().isEmpty()
					&& !this.getFreeVar().isEmpty()) {

				// check type and apply accordingly substitution
				if (this.isVar()) {// variable
					return getVarSubst(new SWRLVar((SWRLVariable) this.argument), subst);

				} else if (this.isTerm()) { // term (atom - unary or binary =
					// class/ objectproperty/ dataproperty expression)
					return new SWRLTerm(getTermSubst(this.atom, subst));
				} else { // rule
					// body for sure contains something
					Set<SWRLAtom> newbody = getRuleSubst(this.rule.getBody(),
							subst);
					// process head if not empty
					Set<SWRLAtom> newhead = getRuleSubst(this.rule.getHead(),
							subst);
					SWRLQuery newquery = new SWRLQuery(df.getSWRLRule(newbody, newhead));
					 return newquery;
				}
			}
		}
		// if substitution is empty, return itself
		// System.out.println("result subst: " + this);
		return this;
	}

	private SWRLTerm getVarSubst(SWRLVar var, SWRLSubstitution subst) {
		if (subst.getSWRLVariables().contains(var)){
			return subst.getSWRLTerm(var);
		}
		return var;
	}

	private SWRLAtom getTermSubst(SWRLAtom atom, SWRLSubstitution subst) {
		Collection<SWRLArgument> args = atom.getAllArguments();
		List<SWRLArgument> newArgs = new LinkedList<SWRLArgument>();
		SWRLPredicate predicate = atom.getPredicate();
		for (SWRLArgument arg : args) {
			// if variable
			if (arg instanceof SWRLVariable) {
				SWRLVar nvar = new SWRLVar((SWRLVariable)arg);

				SWRLTerm newArg = getVarSubst(nvar, subst);

				if (newArg.isArgument()) { // if argument
					if (newArg != null) {
						// add it to the list of new arguments
						newArgs.add(newArg.argument); // the substituted
						// argument
					}
				} else if (newArg.isAtom()) { // if atom
					return newArg.atom;
				}
				else {
					System.out.println("Could not get subst for ARG: " + arg
							+ " - " + newArg + " type "
							+ newArg.getClass().toString());
				}
			} else
				// add it to the list of new arguments
				newArgs.add(arg); // the non-changing argument
		}

		// construct new term to be returned, because we cannot change the
		// existing one
		SWRLAtom newatom = null;
		if (newArgs.size() == 1) {
			if (atom instanceof SWRLClassAtom) // class
				newatom = df.getSWRLClassAtom((OWLClassExpression) predicate,
						(SWRLIArgument) newArgs.get(0));
		} else if (newArgs.size() == 2) {
			if (atom instanceof SWRLDataPropertyAtom) // data property
				newatom = df.getSWRLDataPropertyAtom(
						(OWLDataPropertyExpression) predicate,
						(SWRLIArgument) newArgs.get(0),
						(SWRLDArgument) newArgs.get(1));
			else if (atom instanceof SWRLObjectPropertyAtom) // object property
				newatom = df.getSWRLObjectPropertyAtom(
						(OWLObjectPropertyExpression) predicate,
						(SWRLIArgument) newArgs.get(0),
						(SWRLIArgument) newArgs.get(1));
			// else if (this.atom instanceof SWRLBuiltInAtom) //builtin
			// newatom = df.getSWRLBuiltInAtom((IRI)predicate,
			// (List<SWRLDArgument>)newArgs);
			else if (atom instanceof SWRLDataRangeAtom) // data range
				newatom = df.getSWRLDataRangeAtom((OWLDataRange) predicate,
						(SWRLDArgument) newArgs.get(0));
			else if (atom instanceof SWRLSameIndividualAtom) // same individuals
				newatom = df.getSWRLSameIndividualAtom(
						(SWRLIArgument) newArgs.get(0),
						(SWRLIArgument) newArgs.get(1));
			else if (atom instanceof SWRLDifferentIndividualsAtom)
				// diff individuals
				newatom = df.getSWRLDifferentIndividualsAtom(
						(SWRLIArgument) newArgs.get(0),
						(SWRLIArgument) newArgs.get(1));
		}
		if (newatom != null){
			return newatom;
		}
		return atom;
	}

	private Set<SWRLAtom> getRuleSubst(Set<SWRLAtom> atoms,
			SWRLSubstitution subst) {
		Set<SWRLAtom> newset = new HashSet<SWRLAtom>();
		if (!atoms.isEmpty()) {
			// process the atoms
			for (SWRLAtom atom : atoms) {
				SWRLAtom newatom = getTermSubst(atom, subst);
				if (newatom != null)
					newset.add(newatom);
			}
		}
		return newset;
	}

	protected SWRLSubstitution mguVar(SWRLExpression exp) {
		SWRLArgument thisVar = this.argument;
		SWRLArgument otherArg = exp.argument;
		if (exp.isVar()) {
			// both variables
			return new SWRLSubstitution((SWRLVariable) thisVar,
					(SWRLVariable) otherArg);
		} else if (exp.isArgument()) {
			// variable to argument (literal)
			return new SWRLSubstitution((SWRLVariable) thisVar, otherArg);
		}
		return new SWRLSubstitution();
	}

	protected SWRLSubstitution mguTerm(SWRLExpression exp){
		SWRLSubstitution substitution = new SWRLSubstitution();

		// if both terms (atoms)
		SWRLAtom thisTerm = this.atom;
		SWRLAtom otherTerm = exp.atom;
		if (thisTerm.getPredicate().equals(otherTerm.getPredicate())) {
			// it is the same predicate
			Collection<SWRLArgument> thisArgs = thisTerm.getAllArguments();
			Collection<SWRLArgument> otherArgs =otherTerm.getAllArguments();
			if (thisArgs.size() == otherArgs.size()){
				// We suppose the terms will be unifiable,
				// and check for proven wrong
				boolean unifTerms = true;
				SWRLSubstitution termSubst = new SWRLSubstitution();

				//if they have the same number of arguments
				Iterator<SWRLArgument> it1 = thisArgs.iterator();
				Iterator<SWRLArgument> it2 = otherArgs.iterator();
				while (it1.hasNext() && it2.hasNext()){
					// extract the arguments from both terms (wrap it in terms,
					// as we cannot pass arguments here)
					SWRLTerm argThis = new SWRLTerm(it1.next());
					SWRLTerm argOther = new SWRLTerm(it2.next());
					// if the two arguments are not the same
					if (!argThis.equals(argOther)) {
						if (argThis.isVariable()) {
							// if the first argument is variable, get mguVar
							// with other
							// collect the mgu of two swrl arguments
							termSubst = argThis.mguVar(argOther);
						} else {
							// if this argument is not variable, it is literal
							// argument
							// check if other argument is variable it is fine
							// if not, then other argument is equal literal
							// argument
							if (!argOther.isVariable()) {
								// if not equal literal, than terms are not
								// matching and break
								if (!argThis.equals(argOther))
									//terms are not equal coz this parameter is not matching
									unifTerms=false;
							} else
								// other argument is variable
								termSubst.addSWRLSubstitution(argOther.mguVar(argThis));
						}
					}
				} // end-while
					// now we can check if the terms were ok to unify
				if (unifTerms)
					// add the collected substitution only if all parameters
					// match
					substitution.addSWRLSubstitution(termSubst);

			}
		}
		return substitution;

	}

	protected SWRLSubstitution mguRuleTerm(SWRLExpression exp){
		//this = rule, exp = term
		SWRLSubstitution substitution = new SWRLSubstitution();

		//get mgu of Term-Term for each term in body
		for (SWRLAtom atom : this.rule.getBody())
			substitution.addSWRLSubstitution( (new SWRLTerm(atom)).mguTerm(exp));
		//and in head
		for (SWRLAtom atom : this.rule.getHead())
			substitution.addSWRLSubstitution( (new SWRLTerm(atom)).mguTerm(exp));

		return substitution;
	}

	protected SWRLSubstitution mguRule(SWRLExpression exp) {
		SWRLSubstitution substitution = new SWRLSubstitution();
		//this is rule
		// if other expression is a term (cannot be var or arg)
		if (exp.isTerm()) {
			//get rule-term mgu
			substitution.addSWRLSubstitution(this.mguRuleTerm(exp));

		} else if (exp.isRule()) {
			//both rules, for each atom in other, call mgu of this and atom 
			SWRLRule otherRule = exp.rule;
			for (SWRLAtom atom : otherRule.getBody())
				substitution.addSWRLSubstitution(this.mguRuleTerm(new SWRLTerm(atom)));
			for (SWRLAtom atom : otherRule.getHead())
				substitution.addSWRLSubstitution(this.mguRuleTerm(new SWRLTerm(atom)));
		}

		return substitution;
	}

	public SWRLExpression renameVars() {
		Random randomGenerator = new Random();
		int uniqueId =  randomGenerator.nextInt(100000);
		// rename vars
		Set<Var> freeVars = this.getFreeVar();
		//find where is the variable
		//construct new required terms
		//with new unique variable
		if (this.isVar()){
			SWRLVariable v = (SWRLVariable)this.argument;
			String[] vIri = v.getIRI().toString().split("#");
			if (vIri.length == 2){
				String newv = vIri[0] + "#" + uniqueId + vIri[1];
				return new SWRLVar(newv);
			}
		}else if (this.isTerm()){
			SWRLAtom atom = this.atom;
			List<SWRLArgument> newArgs = new ArrayList<SWRLArgument>();
			for (SWRLArgument arg : atom.getAllArguments()){
				if (arg instanceof SWRLVar)
					newArgs.add((new SWRLTerm(arg).renameVars()).argument);
				newArgs.add(arg);
			}	
			SWRLPredicate pred = atom.getPredicate();
			//depending on the type
			// return new SWRLTerm(df.getSWRL)
		}else if (this.isRule()){

		}

		return this;
	}

	public Substitution mgu(Expression expression) {
		SWRLSubstitution substitution = new SWRLSubstitution();
		SWRLExpression exp = (SWRLExpression) expression;

		// check for variables with the same name - reify
		boolean needReification = false;
		for (Var v : exp.getFreeVar())
			if (this.getFreeVar().contains(v))
				needReification = true;
		for (Var v : this.getFreeVar())
			if (exp.getFreeVar().contains(v))
				needReification = true;
		// buggy for now
		// if (needReification) {
		// this.renameVars().mgu(exp.renameVars());
		// }
		// if expressions are equal, return empty substitution
		if (!this.equals(expression)) {
			// otherwise treat by case
			if (this.isVar()) {
				// get mgu of var/var or var/arg (literal)
				substitution.addSWRLSubstitution(this.mguVar(exp));
			} else if (exp.isVar()) {
				// get mgu of var/var or var/arg (literal)
				substitution.addSWRLSubstitution(exp.mguVar(this));
			} else if (this.isTerm()) {
				if (exp.isTerm()) {
					// both terms and this has variables
					// get mgu for arguments of first with second - use mguVar
					substitution.addSWRLSubstitution(this.mguTerm(exp));
				} else if (exp.isRule()) {
					// term-rule
					substitution.addSWRLSubstitution(exp.mguRuleTerm(this));
				}
			} else if (this.isRule()) {
				// both rules and this has variables
				// get mgu for each term - argument - use mguTerm
				substitution.addSWRLSubstitution(this.mguRule(exp));

			} else if (this.isUndefined() || exp.isUndefined()) {
				// do nothing, return empty
			}
		}
		// call the inverse - stack overflow ?
		// substitution.addSWRLSubstitution((SWRLSubstitution) exp.mgu(this));
		return substitution;

	}

	public Set<SWRLTerm> getTermSet() {
		Set<SWRLTerm> terms = new HashSet<SWRLTerm>();
		if (this.isArgument() || (this.isTerm()))
			terms.add((SWRLTerm) this);
		else {
			Set<SWRLAtom> atoms = rule.getBody();
			atoms.addAll(rule.getHead());
			for (SWRLAtom atom : atoms) {
				terms.add(new SWRLTerm(atom));
			}
		}
		return terms;
	}

	@Override
	public String toString() {
		if (this.rule != null) {
			SWRLTranslator trans = new SWRLTranslator();
			return trans.getRuleText(rule);
		}
		return expression.toString();
	}

	@Override
	public SourceInfo getSourceInfo() {
		// TODO Auto-generated method stub
		return new SWRLParserSourceInfo(null);
	}

	@Override
	public int hashCode() {
		switch (type) {
		case 1:
			return rule.hashCode();
		case 2:
			return atom.hashCode();
		case 3:
			return argument.hashCode();
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SWRLExpression other = (SWRLExpression) obj;
		if (this.type != other.type)
			return false;

		boolean equal = false;
		switch (type) {
		case 1:
			equal = this.rule.equals(other.rule);
			break;
		case 2:
			equal = this.atom.equals(other.atom);
			break;
		case 3:
			equal = this.argument.equals(other.argument);
			break;
		}
		return equal;
	}

}
