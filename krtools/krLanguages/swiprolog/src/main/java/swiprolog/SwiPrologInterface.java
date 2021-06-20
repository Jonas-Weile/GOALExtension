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

package swiprolog;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jpl7.fli.Prolog;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import swiprolog.database.PrologDatabase;
import swiprolog.language.PrologSubstitution;
import swiprolog.parser.Analyzer;
import swiprolog.parser.KRInterfaceParser4;

/**
 * Implementation of {@link KRInterface} for SWI Prolog.
 */
public final class SwiPrologInterface implements KRInterface {
	/**
	 * Contains all databases that are maintained by SWI Prolog. The key is the
	 * owner of the database. The value is a list of databases associated with that
	 * agent (by name).
	 */
	private final Map<String, Map<String, PrologDatabase>> databases = new ConcurrentHashMap<>();

	/**
	 * See {@link SwiInstaller#init(boolean)}.
	 */
	public SwiPrologInterface() {
		try {
			SwiInstaller.init(false);
		} catch (final Throwable retry) {
			SwiInstaller.init(true);
		}
	}

	private Map<String, PrologDatabase> getOrCreateDatabasesForOwner(final String owner) {
		Map<String, PrologDatabase> myDatabases = this.databases.get(owner);
		if (myDatabases == null) {
			myDatabases = new ConcurrentHashMap<>();
			this.databases.put(owner, myDatabases);
			Prolog.create_engine();
		}
		return myDatabases;
	}

	@Override
	public Database getDatabase(final String owner, final String name) throws KRDatabaseException {
		final Map<String, PrologDatabase> myDatabases = getOrCreateDatabasesForOwner(owner);

		PrologDatabase database = myDatabases.get(name);
		if (database == null) {
			database = new PrologDatabase(this, owner, name);
			myDatabases.put(name, database);
		}

		return database;
	}

	@Override
	public Database getDatabase(final String owner, final String name, final Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		final Map<String, PrologDatabase> myDatabases = getOrCreateDatabasesForOwner(owner);

		PrologDatabase database = myDatabases.get(name);
		if (database == null) {
			database = new PrologDatabase(this, owner, name, content);
			myDatabases.put(name, database);
		}

		return database;
	}

	public void removeDatabase(final PrologDatabase db) throws KRDatabaseException {
		final Map<String, PrologDatabase> myDatabases = this.databases.get(db.getOwner());
		if (myDatabases != null) {
			final PrologDatabase removed = myDatabases.remove(db.getName());
			if (removed != null) {
				removed.destroy();
				if (myDatabases.isEmpty()) {
					this.databases.remove(db.getOwner());
					Prolog.destroy_engine();
				}
			}
		}
	}

	@Override
	public Parser getParser(final Reader r, final SourceInfo info) throws ParserException {
		try {
			return new KRInterfaceParser4(r, info);
		} catch (final IOException e) {
			throw new ParserException("failed to parse the reader data as SWI Prolog.", info, e);
		}
	}

	@Override
	public void initialize(final List<URI> uris) throws KRInitFailedException {

	}

	@Override
	public void release() throws KRDatabaseException {
		for (final Map<String, PrologDatabase> databases : this.databases.values()) {
			for (final PrologDatabase db : databases.values()) {
				db.destroy();
			}
		}
		this.databases.clear();
	}

	@Override
	public Substitution getSubstitution(final Map<Var, Term> map) {
		final PrologSubstitution substitution = new PrologSubstitution();
		if (map != null) {
			for (final Var var : map.keySet()) {
				substitution.addBinding(var, map.get(var));
			}
		}
		return substitution;
	}

	@Override
	public Set<Query> getUndefined(final Set<DatabaseFormula> dbfs, final Set<Query> queries) {
		final Analyzer analyzer = new Analyzer(dbfs, queries);
		analyzer.analyze();
		return analyzer.getUndefined();
	}

	@Override
	public Set<DatabaseFormula> getUnused(final Set<DatabaseFormula> dbfs, final Set<Query> queries) {
		final Analyzer analyzer = new Analyzer(dbfs, queries);
		analyzer.analyze();
		return analyzer.getUnused();
	}

	@Override
	public boolean supportsSerialization() {
		return false;
	}
}