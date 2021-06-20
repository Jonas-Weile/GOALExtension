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

package swiprolog.database;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import swiprolog.SwiPrologInterface;
import swiprolog.errors.PrologError;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologSubstitution;
import swiprolog.language.impl.PrologImplFactory;

public class PrologDatabase implements Database {
	private final String owner, name;
	/**
	 * The KRI that is managing this database.
	 */
	private final SwiPrologInterface kri;
	/**
	 * The full name of this database used to name a SWI-Prolog module that
	 * implements the database.
	 */
	private final PrologCompound jplName;
	/**
	 * A corresponding theory.
	 */
	private final Theory theory;
	/**
	 * True iff we are not allowed to modify the initially given content.
	 */
	private final boolean isStatic;
	/**
	 * A cache of write operations. Insert or deletes are queued here until the next
	 * query, at which point those operations are first all performed.
	 */
	private PrologCompound writecache;
	private int cachecount = 0;

	/**
	 * @param kri   The interface instance that creates this database.
	 * @param owner A human-readable name for the owner of the database.
	 * @param name  A human-readable name for the database itself.
	 * @throws KRInitFailedException If the database creation failed.
	 */
	public PrologDatabase(final SwiPrologInterface kri, final String owner, final String name)
			throws KRDatabaseException {
		this.kri = kri;
		this.owner = owner;
		this.name = name;
		this.jplName = PrologImplFactory.getAtom(name, null);
		this.theory = new Theory();
		this.isStatic = false;
	}

	/**
	 * @param kri     The interface instance that creates this database.
	 * @param owner   A human-readable name for the owner of the database.
	 * @param name    A human-readable name for the database itself.
	 * @param content The (final and unmodifiable) content of this database.
	 * @throws KRInitFailedException If the database creation failed.
	 */
	public PrologDatabase(final SwiPrologInterface kri, final String owner, final String name,
			final Collection<DatabaseFormula> content) throws KRDatabaseException {
		this.kri = kri;
		this.owner = owner;
		this.name = name;
		this.jplName = null;
		// Knowledge is only really added in SWI Prolog to each belief and goal base;
		// so here we just temporarily store it in a Theory only!
		this.theory = new Theory();
		this.theory.add(content);
		this.isStatic = true;
	}

	private PrologCompound prefix(final Term term) {
		return PrologImplFactory.getCompound(":", new Term[] { this.jplName, term }, null);
	}

	@Override
	public String getOwner() {
		return this.owner;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @return atom with name of this database
	 */
	public PrologCompound getJPLName() {
		return this.jplName;
	}

	public Theory getTheory() {
		return this.theory;
	}

	/**
	 *
	 * @return true iff this database is static i.e. it can not be modified.
	 */
	public boolean isStatic() {
		return this.isStatic;
	}

	/**
	 * Removes a database from the list of databases maintained by SWI Prolog.
	 */
	@Override
	public void destroy() throws KRDatabaseException {
		if (!this.isStatic) {
			eraseContent();
		}
		this.kri.removeDatabase(this);
	}

	/**
	 * Performs given query on the database. As databases are implemented as modules
	 * in SWI Prolog, a query is constructed that contains a reference to the
	 * corresponding module.
	 *
	 * @param pQuery the query to be performed.
	 * @return set of substitutions satisfying the query.
	 */
	@Override
	public Set<Substitution> query(final Query pQuery) throws KRQueryFailedException {
		final PrologCompound query = ((PrologQuery) pQuery).getCompound();
		final PrologQuery db_query_final = PrologImplFactory.getQuery(prefix(query));
		// Perform the query
		flushWriteCache();
		return rawquery(db_query_final);
	}

	/**
	 * Check that this database can be modified
	 *
	 * @throws KRDatabaseException
	 */
	private void checkModifyable() throws KRDatabaseException {
		if (this.isStatic) {
			throw new KRDatabaseException("Database is static and can not be modified");
		}
	}

	/**
	 * Inserts a set of like {@link #insert(DatabaseFormula)}, but does not add them
	 * to the theory. This makes sure that they will not show up when the set of
	 * formulas in this base is requested, and that they cannot be modified either
	 * (because the theory is always checked for that).
	 *
	 * @param knowledge the set of knowledge that should be imposed on this
	 *                  database.
	 * @throws KRDatabaseException
	 */
	public void addKnowledge(final Set<DatabaseFormula> knowledge) throws KRDatabaseException {
		checkModifyable();
		for (final DatabaseFormula formula : knowledge) {
			insert(((PrologDBFormula) formula).getCompound());
		}
	}

	/**
	 * <p>
	 * Inserts formula into SWI prolog database without any checks. You are
	 * responsible for creating legal SWI prolog query. The formula will be prefixed
	 * with the label of the database: the SWI prolog query will look like <br>
	 * <tt>insert(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 * ASSUMES formula can be argument of assert (fact, rules).
	 *
	 * @param formula is the formula to be inserted into database. appropriate
	 *                database label will be prefixed to your formula
	 * @throws KRDatabaseException
	 */
	@Override
	public boolean insert(final DatabaseFormula formula) throws KRDatabaseException {
		checkModifyable();
		if (this.theory.add(formula)) {
			insert(((PrologDBFormula) formula).getCompound());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Creates JPL term that wraps given term inside "assert(databaseName:term)" for
	 * clauses, and just databaseName:term for directives (without the :-).
	 * <p>
	 * Prefix notation is used below to construct the assert term.
	 * </p>
	 *
	 * @param formula  The JPL term to be inserted.
	 * @param database The database the term should be inserted into.
	 * @throws KRDatabaseException
	 */
	private void insert(final PrologCompound formula) throws KRDatabaseException {
		PrologCompound query;
		if (formula.isDirective()) {
			query = prefix(formula.getArg(0));
		} else {
			query = PrologImplFactory.getCompound("assert", new Term[] { prefix(formula) }, null);
		}
		addToWriteCache(query);
	}

	// ***************** delete methods ****************/

	/**
	 * <p>
	 * Deletes a formula from a SWI Prolog Database. You are responsible for
	 * creating legal SWI prolog query. The formula will be prefixed with the label
	 * of the database: the SWI prolog query will look like <br>
	 * <tt>retract(&lt;database label>:&lt;formula>)</tt>
	 * </p>
	 *
	 * @param formula is the DatabaseFormula to be retracted from SWI. ASSUMES
	 *                formula can be argument of retract (fact, rules). CHECK rules
	 *                need to be converted into string correctly! toString may be
	 *                insufficient for SWI queries
	 * @throws KRDatabaseException
	 */
	@Override
	public boolean delete(final DatabaseFormula formula) throws KRDatabaseException {
		checkModifyable();
		if (this.theory.remove(formula)) {
			delete(((PrologDBFormula) formula).getCompound());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Creates JPL term that wraps given term inside
	 * "retractall(databaseName:term)".
	 * <p>
	 * Prefix notation is used below to construct the retract term.
	 * </p>
	 *
	 * @param formula  The JPL term to be deleted.
	 * @param database The database the term should be deleted from.
	 * @throws KRDatabaseException
	 */
	private void delete(final PrologCompound formula) throws KRDatabaseException {
		final PrologCompound retraction = PrologImplFactory.getCompound("retractall", new Term[] { prefix(formula) },
				null);
		addToWriteCache(retraction);
	}

	/**
	 * <p>
	 * A call to SWI Prolog that converts the solutions obtained into
	 * {@link PrologSubstitution}s.
	 * </p>
	 * <p>
	 * WARNING. this is for internal use in KR implementation only.
	 * </p>
	 *
	 * @param query A JPL query.
	 * @return A set of substitutions, empty set if there are no solutions, and a
	 *         set with the empty substitution if the query succeeds but does not
	 *         return any bindings of variables.
	 * @throws KRQueryFailedException
	 */
	private Set<Substitution> rawquery(final PrologQuery query) throws KRQueryFailedException {
		try { // Get all solutions.
			final Map<String, org.jpl7.Term>[] solutions = query.allSolutions();

			// Convert to PrologSubstitution.
			final Set<Substitution> substitutions = new LinkedHashSet<>(solutions.length);
			for (final Map<String, org.jpl7.Term> solution : solutions) {
				final PrologSubstitution subst = new PrologSubstitution();
				for (final Entry<String, org.jpl7.Term> entry : solution.entrySet()) {
					final Var var = PrologImplFactory.getVar(entry.getKey(), null);
					subst.addBinding(var, (Term) entry.getValue());
				}
				substitutions.add(subst);
			}

			return substitutions;
		} catch (final org.jpl7.PrologException e) {
			throw new PrologError(e);
		} catch (final Throwable e) {
			// catch all other (runtime) exceptions and wrap into checked
			// exception with general message
			throw new KRQueryFailedException("swi prolog says the query " + query + " failed", e);
		}
	}

	/**
	 * <p>
	 * Removes all predicates and clauses from the SWI Prolog database.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: This is not implementable fully in SWI prolog. You can reset
	 * a database to free up some memory, but do not re-use the database. It will
	 * NOT reset the dynamic declarations. This is an issue but the JPL interface to
	 * SWI Prolog does not support removing these. Suggested workaround: After
	 * resetting do not re-use this database but make a new one.
	 * </p>
	 * <p>
	 *
	 * @throws KRDatabaseException
	 */
	protected void eraseContent() throws KRDatabaseException {
		this.writecache = null;
		this.cachecount = 0;

		// Construct jpl term
		final Term predicate = PrologImplFactory.getVar("Predicate", null);
		final Term head = PrologImplFactory.getVar("Head", null);
		final PrologCompound db_head = prefix(head);
		final PrologCompound current = PrologImplFactory.getCompound("current_predicate",
				new Term[] { predicate, head }, null);
		final PrologCompound db_current = prefix(current);
		final PrologCompound built_in_atom = PrologImplFactory.getAtom("built_in", null);
		final PrologCompound built_in = PrologImplFactory.getCompound("predicate_property",
				new Term[] { db_head, built_in_atom }, null);
		final PrologCompound foreign_atom = PrologImplFactory.getAtom("foreign", null);
		final PrologCompound foreign = PrologImplFactory.getCompound("predicate_property",
				new Term[] { db_head, foreign_atom }, null);
		final Term anon = PrologImplFactory.getVar("_", null);
		final PrologCompound imported_from = PrologImplFactory.getCompound("imported_from", new Term[] { anon }, null);
		final PrologCompound imported = PrologImplFactory.getCompound("predicate_property",
				new Term[] { db_head, imported_from }, null);
		final PrologCompound not_built_in = PrologImplFactory.getCompound("not", new Term[] { built_in }, null);
		final PrologCompound not_foreign = PrologImplFactory.getCompound("not", new Term[] { foreign }, null);
		final PrologCompound not_imported = PrologImplFactory.getCompound("not", new Term[] { imported }, null);
		final PrologCompound retract = PrologImplFactory.getCompound("retractall", new Term[] { db_head }, null);
		final PrologCompound conj45 = PrologImplFactory.getCompound(",", new Term[] { not_imported, retract }, null);
		final PrologCompound conj345 = PrologImplFactory.getCompound(",", new Term[] { not_foreign, conj45 }, null);
		final PrologCompound conj2345 = PrologImplFactory.getCompound(",", new Term[] { not_built_in, conj345 }, null);
		final PrologCompound conj = PrologImplFactory.getCompound(",", new Term[] { db_current, conj2345 }, null);
		final PrologQuery query = PrologImplFactory.getQuery(conj);

		try {
			rawquery(query);
		} catch (final KRQueryFailedException e) {
			throw new KRDatabaseException("erasing the contents of database '" + this.name + "' failed.", e);
		}
	}

	// NEW: MERGE ALL ASSERTS AND RETRACTS...
	private void addToWriteCache(final PrologCompound formula) throws KRDatabaseException {
		if (this.writecache == null) {
			this.writecache = formula;
		} else {
			this.writecache = PrologImplFactory.getCompound(",", new Term[] { this.writecache, formula },
					formula.getSourceInfo());
		}
		if (++this.cachecount == Byte.MAX_VALUE) {
			try { // prevents stackoverflows
				flushWriteCache();
			} catch (final KRQueryFailedException e) {
				throw new KRDatabaseException("", e);
			}
		}
	}

	// ... TO EXECUTE THEM ALLTOGETHER AT (BEFORE) THE NEXT QUERY
	private void flushWriteCache() throws KRQueryFailedException {
		if (this.writecache != null) {
			try {
				rawquery(PrologImplFactory.getQuery(this.writecache));
			} finally {
				this.writecache = null;
				this.cachecount = 0;
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.owner, this.name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof PrologDatabase)) {
			return false;
		} else {
			final PrologDatabase other = (PrologDatabase) obj;
			return Objects.equals(this.owner, other.owner) && Objects.equals(this.name, other.name);
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PrologDatabase [owner=").append(this.owner).append(", name=").append(this.name).append("]");
		return builder.toString();
	}
}
