package jasonkri.language;

import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jasonkri.Utils;

import java.util.ArrayList;
import java.util.List;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;

/**
 * A JasonUpdate contains either a {@link LogicalFormula} (the 'atom') or a
 * {@link LogExpr} (a conjunct, or negation). See also
 * {@link Utils#getConjuncts(Term)}) and {@link Utils#isUpdate(Term)}.
 * 
 * @author W.Pasman
 *
 */
public class JasonUpdate extends JasonExpression implements Update {

	// lazy cache.
	private List<DatabaseFormula> addList, deleteList;

	/**
	 * Since an update contains new {@link DatabaseFormula}s possibly wrapped
	 * with not(..), we require Structure objects.
	 * 
	 * @param s
	 *            {@link Structure}s to be added and deleted.
	 * @param i
	 */
	public JasonUpdate(LogicalFormula s, SourceInfo i) {
		super(s, i);
		if (!isUpdate()) {
			throw new IllegalArgumentException("structure " + s
					+ " is not an update"); // bug? Or can this be user err?
		}
	}

	@Override
	public Update applySubst(Substitution substitution) {
		return new JasonUpdate((Structure) substitute(substitution),
				getSourceInfo());
	}

	/**
	 * get the Structure in this formula.
	 */
	public Structure getJasonStructure() {
		return (Structure) getJasonTerm();
	}

	@Override
	public List<DatabaseFormula> getAddList() {
		if (addList == null) {
			addList = new ArrayList<DatabaseFormula>();
			for (Term t : Utils.getConjuncts(getJasonTerm())) {
				if (!Utils.isNegation(t) && !Utils.isTrueLiteral(t)) {
					// We just assume the term can be used as DBFormula.
					addList.add(new JasonDatabaseFormula((LiteralImpl) t,
							getSourceInfo()));
				}
			}
		}
		return addList;
	}

	@Override
	public List<DatabaseFormula> getDeleteList() {

		if (deleteList == null) {
			deleteList = new ArrayList<DatabaseFormula>();
			for (Term t : Utils.getConjuncts(getJasonTerm())) {
				if (Utils.isNegation(t)) {
					// We just guess the terms contain databaseformulas.
					deleteList.add(new JasonDatabaseFormula(
							(LiteralImpl) ((LogExpr) t).getLHS(),
							getSourceInfo()));
				}
			}
		}
		return deleteList;
	}

	@Override
	public Query toQuery() {
		return new JasonQuery(getJasonTerm(), getSourceInfo());
	}

}
