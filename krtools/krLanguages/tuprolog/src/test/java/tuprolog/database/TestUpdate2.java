package tuprolog.database;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.language.DatabaseFormula;
import tuprolog.TuPrologInterface;
import tuprolog.language.PrologDBFormula;

/**
 * Test the {@link UpdateEngine} KNOWLEDGEBASE part. Unfortunately we need also
 * the {@link InferenceEngine} to test if the update really worked so we're
 * partially testing {@link InferenceEngine} as well.
 * <p>
 * See also the comments in {@link TestUpdate}.
 */
public class TestUpdate2 {
	// components enabling us to run the tests...
	private KRInterface language;

	// basic knowledge
	private final alice.tuprolog.Struct k1 = new alice.tuprolog.Struct("aap");
	private final alice.tuprolog.Struct k2 = new alice.tuprolog.Struct("beer");
	private final alice.tuprolog.Struct k3 = new alice.tuprolog.Struct("kat");

	private Database knowledgebase;
	private Database beliefbase;

	@Before
	public void setUp() throws Exception {
		this.language = new TuPrologInterface();
		fillKB();
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
	 * Make standard kb and bb where KB holds two knowledge items
	 *
	 * @throws KRDatabaseException
	 */
	private void fillKB() throws KRDatabaseException {
		Set<DatabaseFormula> kbtheory = new LinkedHashSet<>(2);
		kbtheory.add(new PrologDBFormula(this.k1, null));
		kbtheory.add(new PrologDBFormula(this.k2, null));
		this.knowledgebase = this.language.getDatabase("knowledge", kbtheory);
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>(0));
	}

	/**
	 * alternative fill for KB.
	 *
	 * @throws KRDatabaseException
	 */
	private void fillKB2() throws KRDatabaseException {
		Set<DatabaseFormula> kbtheory2 = new LinkedHashSet<>(1);
		kbtheory2.add(new PrologDBFormula(this.k3, null));
		this.knowledgebase = this.language.getDatabase("knowledge", kbtheory2);
		this.beliefbase = this.language.getDatabase("beliefs", new LinkedHashSet<DatabaseFormula>(0));
	}

	/**
	 * Test initial size of the new databases
	 */
	// TODO FIXME
	// @Test
	// public void testInitialBeliefs() throws Exception {
	// assertEquals(2, beliefbase.getAllSentences().length);
	// assertEquals(0, knowledgebase.getAllSentences().length);
	// }

	/**
	 * Test that the knowledge made it into the BB
	 */
	// TODO FIXME
	// @Test
	// public void testInitialQuery1() throws Exception,
	// KRInitFailedException {
	// PrologQuery query = new PrologQuery(k1);
	// Set<Substitution> sol = beliefbase.query(query);
	// assertEquals(1, sol.size());
	// }

	/**
	 * Delete all databases. Just a smoke test, as we can't do anything with
	 * deleted databases.
	 */
	@Test
	public void testDeleteAll() throws Exception {
		this.beliefbase.destroy();
		this.beliefbase = null;
		this.knowledgebase.destroy();
		this.knowledgebase = null;
	}

	/**
	 * Create new Kb and Bb for SAME AGENT NAME, and check that the new BB has
	 * the new Kb content.
	 */
	@Test
	public void testRecreateKbAndBb() throws Exception {
		this.beliefbase.destroy();
		this.knowledgebase.destroy();

		// ok, now we can recreate the KB. But this time different.
		fillKB2();

		// assertEquals(1, beliefbase.getAllSentences().length);
		// assertEquals(0, knowledgebase.getAllSentences().length);

		// TODO FIXME
		// PrologQuery query = new PrologQuery(k3);
		// Set<Substitution> sol = beliefbase.query(query);
		// assertEquals(1, sol.size());
	}
}
