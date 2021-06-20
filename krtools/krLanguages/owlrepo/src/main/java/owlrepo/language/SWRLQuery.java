package owlrepo.language;

import java.util.HashSet;
import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.sqwrl.SQWRLQuery;

import uk.ac.manchester.cs.owl.owlapi.SWRLRuleImpl;

//import org.openrdf.query.Query;

public class SWRLQuery extends SWRLExpression implements Query {

	private String queryName;

	public SWRLQuery(SQWRLQuery query) {
		super(new SWRLRuleImpl(new HashSet<SWRLAtom>(query.getBodyAtoms()),
				new HashSet<SWRLAtom>(query.getHeadAtoms())));
		this.queryName = query.getQueryName();
	}

	public SWRLQuery(SWRLRule query) {
		super(query);
	}
	
	public SWRLQuery(SWRLAtom term){
		super(term);
	}

	public SWRLQuery(SWRLArgument arg) {
		super(arg);
	}

	public SWRLQuery addNamedGraph(String id){
		//TODO
		SWRLAtom atom = createNamedGraphAtom(id);
		Set<SWRLAtom> newbody = this.rule.getBody();
		newbody.add(atom);
		this.rule = df.getSWRLRule(newbody, this.rule.getHead());
		//System.out.println("Named graph atom: "+atom+" was added to "+this);
		return this;
	}
	
	public SWRLQuery removeNamedGraph(){
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
	 * used only for creating a query out of an undefined term string that the
	 * parser could not parse (not in ontology) used only by
	 * OWLRepoKRInterface.getUndefined
	 * 
	 * @param string
	 */
	public SWRLQuery(String string) {
		super(string);
	}

	public String getQueryName() {
		return this.queryName;
	}

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables
	 * that are bound to a term in the substitution by that term (or, only
	 * renames in case the substitution binds a variable to another one).
	 */
	public Query applySubst(Substitution substitution) {
		return (SWRLQuery) super.applySubst(substitution);
	}

	/**
	 * Converts a {@link SWRLQuery} to an {@link SWRLUpdate}.
	 * 
	 * TODO:
	 * <p>
	 * All Mental Literals contain Queries. However goals are represented by
	 * Updates. In order to convert a mental literal into a goal, as is
	 * necessary when instantiating a module, a way to convert a Query into an
	 * Update is needed.
	 * </p>
	 * 
	 * @return An Update with an empty delete list and an add list with the
	 *         content of this {@link SWRLQuery}.
	 */
	public Update toUpdate() {
		if (this.isArgument())
			return new SWRLUpdate(this.argument);
		else if (this.isTerm())
			return new SWRLUpdate(this.atom);
		else 
			return new SWRLUpdate(rule);
	}

	// @Override
	public boolean isUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

}
