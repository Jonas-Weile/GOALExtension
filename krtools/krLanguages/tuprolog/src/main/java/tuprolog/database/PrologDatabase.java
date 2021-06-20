/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package tuprolog.database;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import alice.tuprolog.PrologException;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import tuprolog.TuPrologInterface;
import tuprolog.errors.PrologError;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologDBFormula;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologSubstitution;

public class PrologDatabase implements Database {
	private final alice.tuprolog.Prolog engine;
	private final String name;
	/**
	 * The KRI that is managing this database.
	 */
	private final TuPrologInterface owner;
	/**
	 * A corresponding theory
	 */
	private final Theory theory;

	/**
	 * @param name
	 *            A human-readable name for the database.
	 * @param content
	 *            The theory containing initial database contents.
	 * @param owner
	 *            The interface instance that creates this database.
	 * @throws KRInitFailedException
	 *             If database creation failed.
	 */
	public PrologDatabase(String name, Collection<DatabaseFormula> content, TuPrologInterface owner)
			throws KRDatabaseException {
		this.engine = new alice.tuprolog.Prolog();
		this.name = name;
		this.owner = owner;
		this.theory = new Theory(content);
		if (content != null) {
			for (DatabaseFormula dbf : content) {
				insert(((PrologDBFormula) dbf).getTerm());
			}
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	public Theory getTheory() {
		return this.theory;
	}

	/**
	 * Removes a database from the list of databases maintained by TU Prolog.
	 *
	 * TRAC #2027 there seems no factory pattern dealing with instance deletion.
	 *
	 * @throws KRDatabaseException
	 */
	@Override
	public void destroy() throws KRDatabaseException {
		eraseContent();
		this.owner.removeDatabase(this);
	}

	/**
	 * Performs given query on the database. As databases are implemented as
	 * modules in TU Prolog, a query is constructed that contains a reference to
	 * the corresponding module.
	 *
	 * @param pQuery
	 *            the query to be performed.
	 * @return set of substitutions satisfying the query.
	 */
	@Override
	public Set<Substitution> query(Query pQuery) throws KRQueryFailedException {
		Set<Substitution> substSet = new LinkedHashSet<>();
		alice.tuprolog.Term query = ((PrologQuery) pQuery).getTerm();
		// We need to create conjunctive query with "true" as first conjunct and
		// db_query as second conjunct as JPL query dbname:not(..) does not work
		// otherwise...
		substSet.addAll(rawquery(JPLUtils.createCompound(",", new alice.tuprolog.Struct("true"), query)));
		return substSet;
	}

	/**
	 * Inserts a set of like {@link #insert(DatabaseFormula)}, but does not add
	 * them to the theory. This makes sure that they will not show up when the
	 * set of formulas in this base is requested, and that they cannot be
	 * modified either (because the theory is always checked for that).
	 *
	 * @param knowledge
	 *            the set of knowledge that should be imposed on this database.
	 * @throws KRDatabaseException
	 */
	public void addKnowledge(Set<DatabaseFormula> knowledge) throws KRDatabaseException {
		for (DatabaseFormula formula : knowledge) {
			insert(((PrologDBFormula) formula).getTerm());
		}
	}

	/**
	 * <p>
	 * Inserts formula into TU prolog database without any checks. You are
	 * responsible for creating legal TU prolog query. The formula will be
	 * prefixed with the label of the database: the TU prolog query will look
	 * like <br>
	 * <tt>insert(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 * ASSUMES formula can be argument of assert (fact, rules).
	 *
	 * @param formula
	 *            is the PrologTerm to be inserted into database. appropriate
	 *            database label will be prefixed to your formula
	 * @throws KRDatabaseException
	 */
	@Override
	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		if (this.theory.add(formula)) {
			insert(((PrologDBFormula) formula).getTerm());
		}
	}

	/**
	 * Inserts positive literals that are part of the update into the database
	 * and retracts the negative literals. CHECK maybe it is faster to compile
	 * to a single large JPL call before
	 *
	 * @param update
	 *            The basic databaseformula, not(databaseformula) or
	 *            comma-separated list of dtabase formulas. comma-separated as
	 *            usual in Prolog: (t1,(t2,(t3,(t4...,(..,tn)))))
	 * @param database
	 *            The database into which the update has been inserted.
	 * @throws KRDatabaseException
	 */
	@Override
	public void insert(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getDeleteList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getAddList()) {
			insert(formula);
		}
	}

	/**
	 * Creates JPL term that wraps given term inside "assert(databaseName:term)"
	 * for clauses, and just databaseName:term for directives (without the :-).
	 * <p>
	 * Prefix notation is used below to construct the assert term.
	 * </p>
	 *
	 * @param formula
	 *            The JPL term to be inserted.
	 * @param database
	 *            The database the term should be inserted into.
	 * @throws KRDatabaseException
	 */
	private void insert(alice.tuprolog.Term formula) throws KRDatabaseException {
		try {
			if (formula instanceof alice.tuprolog.Struct && ((alice.tuprolog.Struct) formula).getName().equals(":-")
					&& ((alice.tuprolog.Struct) formula).getArity() == 1) { // directive
				alice.tuprolog.Term query = ((alice.tuprolog.Struct) formula).getArg(0);
				alice.tuprolog.Term queryt = JPLUtils.createCompound(",", new alice.tuprolog.Struct("true"), query);
				rawquery(queryt);
			} else { // clause
				rawquery(JPLUtils.createCompound("assert", formula));
			}
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("inserting '" + formula + "' failed.", e);
		}
	}

	// ***************** delete methods ****************/

	@Override
	public void delete(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getAddList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getDeleteList()) {
			insert(formula);
		}
	}

	/**
	 * <p>
	 * Deletes a formula from a TU Prolog Database. You are responsible for
	 * creating legal TU prolog query. The formula will be prefixed with the
	 * label of the database: the TU prolog query will look like <br>
	 * <tt>retract(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 *
	 * @param formula
	 *            is the DatabaseFormula to be retracted from TU. ASSUMES
	 *            formula can be argument of retract (fact, rules). CHECK rules
	 *            need to be converted into string correctly! toString may be
	 *            insufficient for TU queries
	 * @throws KRDatabaseException
	 */
	@Override
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		if (this.theory.remove(formula)) {
			delete(((PrologDBFormula) formula).getTerm());
		}
	}

	/**
	 * Creates JPL term that wraps given term inside
	 * "retract(databaseName:term)".
	 * <p>
	 * Prefix notation is used below to construct the retract term.
	 * </p>
	 *
	 * @param formula
	 *            The JPL term to be deleted.
	 * @param database
	 *            The database the term should be deleted from.
	 * @throws KRDatabaseException
	 */
	private void delete(alice.tuprolog.Term formula) throws KRDatabaseException {
		try {
			rawquery(JPLUtils.createCompound("retract", formula));
		} catch (KRQueryFailedException e) {
			throw new KRDatabaseException("deleting '" + formula + "' failed.", e);
		}
	}

	/**
	 * <p>
	 * A call to TU Prolog that converts the solutions obtained into
	 * {@link PrologSubstitution}s.
	 * </p>
	 * <p>
	 * WARNING. this is for internal use in KR implementation only. There is a
	 * known issue with floats (TRAC #726).
	 * </p>
	 *
	 * @param query
	 *            A JPL query.
	 * @return A set of substitutions, empty set if there are no solutions, and
	 *         a set with the empty substitution if the query succeeds but does
	 *         not return any bindings of variables.
	 * @throws KRQueryFailedException
	 */
	protected Set<PrologSubstitution> rawquery(alice.tuprolog.Term query) throws KRQueryFailedException {
		try {
			Set<PrologSubstitution> substitutions = new LinkedHashSet<>();

			alice.tuprolog.SolveInfo info = this.engine.solve(query.toString() + ".");
			while (info.isSuccess()) {
				Map<String, alice.tuprolog.Term> solution = new HashMap<>();
				for (alice.tuprolog.Var var : info.getBindingVars()) {
					solution.put(var.getName(), info.getVarValue(var.getName()));
				}
				substitutions.add(PrologSubstitution.getSubstitutionOrNull(solution));
				if (this.engine.hasOpenAlternatives()) {
					info = this.engine.solveNext();
				} else {
					break;
				}
			}

			return substitutions;
		} catch (PrologException e) {
			throw new PrologError(e);
		} catch (Throwable e) {
			// catch all other (runtime) exceptions and wrap into checked
			// exception with general message
			throw new KRQueryFailedException("tu prolog says the query '" + query + "' failed", e);
		}
	}

	/**
	 * <p>
	 * Removes all predicates and clauses from the TU Prolog database.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: This is not implementable fully in TU prolog. You can
	 * reset a database to free up some memory, but do not re-use the database.
	 * It will NOT reset the dynamic declarations. This is an issue but the JPL
	 * interface to TU Prolog does not support removing these. Suggested
	 * workaround: After resetting do not re-use this database but make a new
	 * one.
	 * </p>
	 * <p>
	 *
	 * @throws KRDatabaseException
	 */
	protected void eraseContent() throws KRDatabaseException {
		this.engine.clearTheory();
	}

	@Override
	public String toString() {
		return "<" + this.name + ">";
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PrologDatabase other = (PrologDatabase) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (this.name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
