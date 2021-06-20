package jasonkri;

import jason.asSemantics.Unifier;
import jason.asSyntax.VarTerm;
import jasonkri.language.JasonSubstitution;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonVar;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

public class JasonInterface implements KRInterface {

	private Map<String, JasonDatabase> databases;

	@Override
	public void initialize(List<URI> uris) throws KRInitFailedException {
		reset();
	}

	@Override
	public void release() throws KRDatabaseException {
		reset();
	}

	/**
	 * remove all databases. Free all memory.
	 */
	private void reset() {
		databases = new ConcurrentHashMap<String, JasonDatabase>();
	}

	@Override
	public Database getDatabase(String name, Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		JasonDatabase db = new JasonDatabase(content);
		databases.put(name, db);
		return db;
	}

	@Override
	public Parser getParser(Reader source, SourceInfo info)
			throws ParserException {
		return new JasonParser(source, info);
	}

	@Override
	public Substitution getSubstitution(Map<Var, Term> map) {
		Unifier unifier = new Unifier();
		if (map != null) {
			for (Var var : map.keySet()) {
				unifier.bind((VarTerm) ((JasonVar) var).getJasonTerm(),
						((JasonTerm) map.get(var)).getJasonTerm());
			}
		}
		return new JasonSubstitution(unifier);
	}

	@Override
	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		return new HashSet<Query>(); // FIXME
	}

	@Override
	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		return new HashSet<DatabaseFormula>(); // FIXME
	}

	@Override
	public boolean supportsSerialization() {
		return false; // Can be fixed. Is it needed?
	}

}
