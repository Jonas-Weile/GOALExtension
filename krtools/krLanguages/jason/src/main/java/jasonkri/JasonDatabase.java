package jasonkri;

import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonSubstitution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;

public class JasonDatabase implements Database {

	private static long uniqueNumberCounter = 0;
	private final String name;
	private BeliefBase database = new DefaultBeliefBase();

	/**
	 * @param content
	 *            the content to add. If content is null, it will be ignored.
	 * 
	 * @throws krTools.exceptions.KRDatabaseException
	 */
	public JasonDatabase(Collection<DatabaseFormula> content)
			throws krTools.exceptions.KRDatabaseException {
		name = "jasondbase" + uniqueNumberCounter++;
		if (content != null) {
			addAll(content);
		}
	}

	/**
	 * load with already existing {@link BeliefBase}.
	 * 
	 * @param data
	 *            {@link BeliefBase}
	 */
	public JasonDatabase(BeliefBase data) {
		name = "jasondbase" + uniqueNumberCounter++;
		database = data;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<Substitution> query(Query query) throws KRQueryFailedException {
		/**
		 * a query is guaranteed to be a LogicalFormula, see
		 * {@link Utils#isQuery}.
		 */
		Term term = ((JasonQuery) query).getJasonTerm();
		Iterator<Unifier> result = ((LogicalFormula) term).logicalConsequence(
				database, new Unifier());

		Set<Substitution> solutions = new HashSet<Substitution>();
		while (result.hasNext()) {
			solutions.add(new JasonSubstitution(result.next()));
		}
		return solutions;
	}

	@Override
	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		database.add(((JasonDatabaseFormula) formula).getJasonLiteral());

	}

	@Override
	public void insert(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getDeleteList()) {
			delete((formula));
		}
		for (DatabaseFormula formula : update.getAddList()) {
			insert((formula));
		}
	}

	@Override
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		database.remove(((JasonDatabaseFormula) formula).getJasonLiteral());

	}

	@Override
	public void delete(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getAddList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getDeleteList()) {
			insert(formula);
		}

	}

	@Override
	public void destroy() throws KRDatabaseException {
		database = new DefaultBeliefBase();
	}

	/**
	 * Make a clone.
	 * 
	 * @return clone of this database.
	 */
	public JasonDatabase duplicate() {
		return new JasonDatabase(database.clone());
	}

	public void addAll(Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		for (DatabaseFormula formula : content) {
			insert(formula);
		}

	}

	/**
	 * 
	 * @return the database contents.
	 */
	public List<LiteralImpl> getContents() {
		List<LiteralImpl> literals = new ArrayList<LiteralImpl>();
		for (Literal l : database) {
			// #3582 BeliefBase contains Literals but only LiteralImpl and some
			// derived classes can actually be added.
			literals.add((LiteralImpl) l);
		}
		return literals;
	}

	@Override
	public String toString() {
		return database.toString();
	}
}
