package owlrepo.language;

import java.util.LinkedList;
import java.util.List;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;

public class SWRLUpdate extends SWRLExpression implements Update {

	public SWRLUpdate(SWRLRule axiom) {
		super(axiom);
	}

	public SWRLUpdate(SWRLAtom atom) {
		super(atom);
	}

	public SWRLUpdate(SWRLArgument term) {
		super(term);
	}

	public SWRLUpdate(String s) {
		super(s);
	}

	/**
	 * Applies a substitution to the term, i.e., instantiates free variables
	 * that are bound to a term in the substitution by that term (or, only
	 * renames in case the substitution binds a variable to another one).
	 */
	@Override
	public Update applySubst(Substitution substitution) {
		SWRLExpression exp = (SWRLExpression) super.applySubst(substitution);
		if (exp.isRule())
			if (exp instanceof SWRLUpdate)
				return (SWRLUpdate)exp;
			else if (exp instanceof SWRLQuery)
				return ((SWRLQuery) exp).toUpdate();
		else if (exp.isArgument())
			return new SWRLUpdate(exp.argument);
		return this;
	}

	@Override
	public List<DatabaseFormula> getAddList() {
		LinkedList<DatabaseFormula> addlist = new LinkedList<DatabaseFormula>();
		if (this.isRule()) {
			addlist.add(new SWRLDatabaseFormula(this.rule));
		} else if (this.isTerm()) {
			addlist.add(new SWRLDatabaseFormula(this.atom));
		} else if (this.isArgument()) {
			addlist.add(new SWRLDatabaseFormula(this.argument));
		}
		return addlist;
	}

	@Override
	public List<DatabaseFormula> getDeleteList() {
		// TODO Auto-generated method stub
		return new LinkedList<DatabaseFormula>();
	}

	@Override
	public Query toQuery() {
		// TODO Auto-generated method stub
		return new SWRLQuery(this.rule);
	}

	// @Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

}
