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

package krTools;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * The knowledge representation (KR) interface.
 *
 * <p>
 * A knowledge representation interface should provide the following services:
 * <ul>
 * <li>initializing the interface with a list of references to enable its use.
 * </li>
 * <li>resetting the interface.</li>
 * <li>creating a database.</li>
 * <li>providing a parser for parsing source (files) represented in the KR
 * language.</li>
 * <li>mapping a map of variables and terms to a substitution.</li>
 * <li>getting the queries from the given set of queries that have not been
 * defined.</li>
 * <li>getting the formulas that have been defined in the set of database
 * formulas but are not used (queried).</li>
 * <li>checking if the expressions of the KR Language support serialization.
 * </li>
 * </ul>
 *
 * Logging from implementors can be done through the "KRLogger", got by using
 * {@link Logger#getLogger(String)}.
 */
public interface KRInterface {

	/**
	 * Performs any initializations that need to be performed before the KR
	 * interface can be used. Pass on any URI (file or URL) from use cases that is
	 * needed to initialize the interface.
	 *
	 * @param set of URI-s that can be files, url-s or urn-s to be passed
	 * @throws KRInitFailedException If initialization of the KR interface failed.
	 */
	void initialize(List<URI> uris) throws KRInitFailedException;

	/**
	 * Resets the KR interface. Should clear and free all memory used by the
	 * inference engine and any of the databases that have been created.
	 *
	 * @throws KRDatabaseException If releasing resources was not successful or
	 *                             incomplete.
	 */
	void release() throws KRDatabaseException;

	/**
	 * Creates new database from the content provided. This initial content will be
	 * unmodifiable.
	 *
	 * @param owner   A human-readable name for the owner of the database.
	 * @param name    A human-readable name for the database itself.
	 * @param content A list of {@link DatabaseFormula}s that should be inserted in
	 *                the database. Any attempted change to this content will throw
	 *                an exception.
	 * @return A database with the given content.
	 * @throws KRDatabaseException If the database could not be created or the
	 *                             content could not be added.
	 */
	Database getDatabase(String owner, String name, Collection<DatabaseFormula> content) throws KRDatabaseException;

	/**
	 * Creates new empty but modifiable database.
	 *
	 * @param owner A human-readable name for the owner of the database.
	 * @param name  A human-readable name for the database itself.
	 * @return An empty database that can be modified.
	 * @throws KRDatabaseException If the database could not be created.
	 */
	Database getDatabase(String owner, String name) throws KRDatabaseException;

	/**
	 * Returns a parser for this KR language. Only initializes the parser but does
	 * not parse the source yet. See {@link Parser} for methods that parse the
	 * input.
	 *
	 * @param source A reader of the source that is to be parsed.
	 * @param info   the {@link SourceInfo}. This is needed as this parser will be
	 *               used as subparser, and then it needs to be able to create
	 *               correct source references and error messages with correct line
	 *               numbers.
	 * @throws ParserException If anything went wrong during initialization of the
	 *                         parser, e.g., due to a problem with the source.
	 */
	Parser getParser(Reader source, SourceInfo info) throws ParserException;

	/**
	 * Creates a substitution from a map of variables to terms.
	 *
	 * @param map The Var to Term mapping; possibly null.
	 *
	 * @return A substitution which binds all variables in the map to the associated
	 *         terms.
	 */
	Substitution getSubstitution(Map<Var, Term> map);

	/**
	 * Reports the queries in the given set of queries that have not been defined in
	 * the set of database formulas as well as those queries that are implicitly
	 * queries in the set of database formulas itself (e.g., as condition in a
	 * rule).
	 */
	Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries);

	/**
	 * @param dbfs    the defined database formulas
	 * @param queries the queries that are actually done
	 * @return the formulas that have been defined in the set of database formulas
	 *         but are not used (queried).
	 */
	Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs, Set<Query> queries);

	/**
	 * Check if the expressions of this KR implementation can be serialized
	 * (converted into a byte stream for easy transmission and then converted back
	 * into the original object).
	 *
	 * @return true iff terms from this KR implementation can be serialized
	 */
	boolean supportsSerialization();
}
