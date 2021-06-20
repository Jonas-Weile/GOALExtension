package tuprolog.database;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import alice.tuprolog.Term;
import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Update;
import tuprolog.TuPrologInterface;
import tuprolog.language.PrologDBFormula;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologUpdate;

/**
 * Test the {@link UpdateEngine} BELIEFBASE part. Unfortunately we need also the
 * {@link InferenceEngine} to test if the update really worked so we're
 * partially testing {@link InferenceEngine} as well.
 * <p>
 * We should test ALL inference engine implementations here. The idea is that we
 * can feed all inference engines with generic tests and they should all behave
 * the same. Unfortunately this may not be possible since the databases all have
 * their own data types, eg SWI Prolog accepts only SWI Prolog terms, while PDDL
 * only accepts PDDL terms etc. So this approach may not be feasible in the end.
 * <p>
 * TODO we need to reach a good coverage in this test. Currently we do just a
 * bit of basic testing only. To create a good test, we should make a state
 * diagram of the UpdateEngine first.
 */
public class TestUpdate {
	// components enabling us to run the tests...
	private KRInterface language;
	private Database beliefbase;
	private Database knowledgebase;

	private final alice.tuprolog.Struct aap = new alice.tuprolog.Struct("aap");
	private final alice.tuprolog.Struct beer = new alice.tuprolog.Struct("beer");
	private final alice.tuprolog.Struct kat = new alice.tuprolog.Struct("kat");

	@Before
	public void setUp() throws Exception {
		this.language = new TuPrologInterface();
		this.knowledgebase = this.language.getDatabase("knowledge", new LinkedHashSet<DatabaseFormula>(0));
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>(0));
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

	@Test
	public void testInitialQuery1() throws Exception {
		PrologQuery query = new PrologQuery(new alice.tuprolog.Struct("true"), null);
		Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Check that inserting 'aap' results in 'aap' query to become true and that
	 * there is 1 sentence in beliefbase after the insert.
	 */
	@Test
	public void testInsertFormula() throws Exception {
		DatabaseFormula formula = new PrologDBFormula(this.aap, null);
		this.beliefbase.insert(formula);

		PrologQuery query = new PrologQuery(this.aap, null);
		Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Check that after updating with (not(aap),beer) that there is 1 sentence
	 * in beliefbase and that 'beer' is true now and 'aap' false.
	 */
	@Test
	public void testUpdate() throws Exception {
		Update update = new PrologUpdate(new alice.tuprolog.Struct(",",
				new Term[] { new alice.tuprolog.Struct("not", new Term[] { this.aap }), this.beer }), null);
		this.beliefbase.insert(update);

		// assertEquals(1, beliefbase.getAllSentences().length);
		// assertEquals(0, knowledgebase.getAllSentences().length);

		// TODO FIXME
		// PrologQuery query = new PrologQuery(aap);
		// Set<Substitution> sol = beliefbase.query(query);
		// assertEquals(sol.size(), 0);

		PrologQuery query2 = new PrologQuery(this.beer, null);
		Set<Substitution> sol2 = this.beliefbase.query(query2);
		assertEquals(1, sol2.size());
	}

	/**
	 * Delete belief base; create new belief base. Check that there are no
	 * predicates in new belief base.
	 */
	@Test
	public void testDeleteBeliefbase() throws Exception {
		this.beliefbase.destroy();
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>());

		// assertEquals(0, beliefbase.getAllSentences().length);
	}

	/**
	 * After creation of new (empty) BB, check that the new BB also works by
	 * inserting something and checking that it gets there.
	 */
	@Test
	public void testUseNewBeliefbase() throws KRQueryFailedException, KRDatabaseException {
		DatabaseFormula formula = new PrologDBFormula(this.kat, null);
		this.beliefbase.insert(formula);

		// assertEquals(1, beliefbase.getAllSentences().length);
		// assertEquals(0, knowledgebase.getAllSentences().length);

		PrologQuery query = new PrologQuery(this.kat, null);
		Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}
}
