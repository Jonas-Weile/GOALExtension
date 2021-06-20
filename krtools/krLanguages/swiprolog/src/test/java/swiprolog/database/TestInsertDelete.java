package swiprolog.database;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.Substitution;
import krTools.language.Term;
import swiprolog.SwiPrologInterface;
import swiprolog.errors.PrologError;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;

/**
 * Test inserts and deletes in a database.
 */
public class TestInsertDelete {
	// components enabling us to run the tests...
	private static KRInterface language = new SwiPrologInterface();
	private Database beliefbase;
	private Database knowledgebase;
	// beliefs
	private PrologCompound p1;
	private PrologCompound p2;
	private PrologCompound dynamicp;

	@Before
	public void setUp() throws Exception {
		final String agentName = TestInsertDelete.class.getSimpleName();
		this.knowledgebase = language.getDatabase(agentName, "knowledge", new ArrayList<>(0));
		this.beliefbase = language.getDatabase(agentName, "beliefs");
		this.p1 = PrologImplFactory.getAtom("p", null);
		this.p2 = PrologImplFactory.getAtom("p", null);
		final PrologCompound pSig = PrologImplFactory.getCompound("/",
				new Term[] { PrologImplFactory.getAtom("p", null), PrologImplFactory.getNumber(0, null) }, null);
		this.dynamicp = PrologImplFactory.getCompound("thread_local", new Term[] { pSig }, null);
		this.beliefbase.query(PrologImplFactory.getQuery(this.dynamicp));
	}

	@After
	public void tearDown() throws Exception {
		if (this.beliefbase != null) {
			this.beliefbase.destroy();
		}
		if (this.knowledgebase != null) {
			this.knowledgebase.destroy();
		}
	}

	/**
	 * Query p
	 *
	 * @return set of solutions for query p.
	 */
	Set<Substitution> QueryP() throws KRQueryFailedException {
		return this.beliefbase.query(PrologImplFactory.getQuery(this.p1));
	}

	@Test
	public void testInsert() throws KRDatabaseException, KRQueryFailedException {
		assertEquals(0, QueryP().size());

		this.beliefbase.insert(PrologImplFactory.getDBFormula(this.p1));

		assertEquals(1, QueryP().size());
	}

	/**
	 * You can insert duplicates. But you won't see them as query returns a SET
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testInsertDuplicate() throws KRDatabaseException, KRQueryFailedException {
		assertEquals(0, QueryP().size());

		this.beliefbase.insert(PrologImplFactory.getDBFormula(this.p1));
		this.beliefbase.insert(PrologImplFactory.getDBFormula(this.p2));

		assertEquals(1, QueryP().size());
	}

	/**
	 * Check that delete deletes ALL duplicates.
	 *
	 * @throws KRDatabaseException
	 * @throws KRQueryFailedException
	 */
	@Test
	public void testDeleteAfterDuplicate() throws KRDatabaseException, KRQueryFailedException {
		testInsertDuplicate();
		this.beliefbase.delete(PrologImplFactory.getDBFormula(this.p1));
		assertEquals(0, QueryP().size());
	}

	@Test
	@SuppressWarnings("deprecation") // FIXME
	public void testDatabaseErase() throws KRDatabaseException, KRQueryFailedException {
		final String stringterm = "requests([request('INTERACTION', 2,'.'(answer(0, 'OK'), [])),request('INTERACTION', 3, '.'(answer(0,'OK'), []))])";
		final org.jpl7.Term t = org.jpl7.Util.textToTerm(stringterm);
		this.beliefbase.insert(PrologImplFactory.getDBFormula((PrologCompound) PrologError.fromJpl(t)));

		final org.jpl7.Term queryterm = org.jpl7.Util.textToTerm("requests(X)");
		final PrologQuery query = PrologImplFactory.getQuery((PrologCompound) PrologError.fromJpl(queryterm));
		assertEquals(1, this.beliefbase.query(query).size());

		this.beliefbase.destroy();
		assertEquals(0, this.beliefbase.query(query).size());
	}
}
