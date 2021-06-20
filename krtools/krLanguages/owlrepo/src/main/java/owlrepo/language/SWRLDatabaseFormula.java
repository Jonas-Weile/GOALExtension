package owlrepo.language;

import java.util.HashSet;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;

public class SWRLDatabaseFormula extends SWRLExpression implements
		DatabaseFormula {

	public SWRLDatabaseFormula(OWLAxiom axiom) {
		super(axiom);
	}

	public SWRLDatabaseFormula(SWRLRule rule) {
		super(rule);
	}

	public SWRLDatabaseFormula(SWRLAtom atom) {
		super(atom);
	}

	public SWRLDatabaseFormula(SWRLArgument arg) {
		super(arg);
	}
	
	public SWRLDatabaseFormula addNamedGraph(String id){
		//TODO
		SWRLAtom atom = createNamedGraphAtom(id);
	//	System.out.println("Adding named graph atom: " + atom + "  to " + this);

		Set<SWRLAtom> newbody = this.rule.getBody();
		if (newbody == null)
			newbody = new HashSet<SWRLAtom>();
		newbody.add(atom);
		this.rule = df.getSWRLRule(newbody, this.rule.getHead());
		//System.out.println("Named graph atom: "+atom+" was added to "+this);
		return this;
	}
	
	public SWRLDatabaseFormula removeNamedGraph(){
		//TODO
		SWRLAtom atom = getNamedGraphAtom();
		Set<SWRLAtom> newbody = this.rule.getBody();
		newbody.remove(atom);
		this.rule = df.getSWRLRule(newbody, this.rule.getHead());
		//System.out.println("Named graph atom: "+atom+" was removed from "+this);
		return this;
	}
	
	public String getNamedGraph(){
		//TODO
		SWRLAtom atom = getNamedGraphAtom();
		if (this.rule.getBody().contains(atom))
			return atom.getIndividualsInSignature().iterator().next().toStringID();
		return "";
	}
	
	/**
	 * Applies a substitution to the term, i.e., instantiates free variables
	 * that are bound to a term in the substitution by that term (or, only
	 * renames in case the substitution binds a variable to another one).
	 */
	@Override
	public DatabaseFormula applySubst(Substitution substitution) {
		SWRLExpression exp = (SWRLExpression) super.applySubst(substitution);
		if (exp.isRule())
			return new SWRLDatabaseFormula(exp.rule);
		else if (exp.isArgument())
			return new SWRLDatabaseFormula(exp.argument);
		return this;
	}

	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

	// @Override
	public Query toQuery() {
		// TODO check if its all cases
		return new SWRLQuery(this.rule);
	}

}
