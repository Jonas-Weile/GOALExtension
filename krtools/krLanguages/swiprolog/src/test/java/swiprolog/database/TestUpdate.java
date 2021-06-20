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
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;

public class TestUpdate {
	// components enabling us to run the tests...
	private KRInterface language;
	private String agentName;
	private Database beliefbase;
	private Database knowledgebase;
	// beliefs
	private PrologCompound aap;
	private PrologCompound kat;

	@Before
	public void setUp() throws Exception {
		this.language = new SwiPrologInterface();
		this.agentName = TestUpdate.class.getSimpleName();
		this.knowledgebase = this.language.getDatabase(this.agentName, "knowledge", new ArrayList<>(0));
		this.beliefbase = this.language.getDatabase(this.agentName, "beliefs");
		this.aap = PrologImplFactory.getAtom("aap", null);
		this.kat = PrologImplFactory.getAtom("kat", null);
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
		final PrologQuery query = PrologImplFactory.getQuery(PrologImplFactory.getAtom("true", null));
		final Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Check that inserting 'aap' results in 'aap' query to become true and that
	 * there is 1 sentence in beliefbase after the insert.
	 */
	@Test
	public void testInsertFormula() throws Exception {
		final DatabaseFormula formula = PrologImplFactory.getDBFormula(this.aap);
		this.beliefbase.insert(formula);

		final PrologQuery query = PrologImplFactory.getQuery(this.aap);
		final Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}

	/**
	 * Delete belief base; create new belief base. Check that there are no
	 * predicates in new belief base.
	 */
	@Test
	public void testDeleteBeliefbase() throws Exception {
		this.beliefbase.destroy();
		this.beliefbase = this.language.getDatabase(this.agentName, "beliefs");

		// assertEquals(0, beliefbase.getAllSentences().length);
	}

	/**
	 * After creation of new (empty) BB, check that the new BB also works by
	 * inserting something and checking that it gets there.
	 */
	@Test
	public void testUseNewBeliefbase() throws KRQueryFailedException, KRDatabaseException {
		final DatabaseFormula formula = PrologImplFactory.getDBFormula(this.kat);
		this.beliefbase.insert(formula);

		// assertEquals(1, beliefbase.getAllSentences().length);
		// assertEquals(0, knowledgebase.getAllSentences().length);

		final PrologQuery query = PrologImplFactory.getQuery(this.kat);
		final Set<Substitution> sol = this.beliefbase.query(query);
		assertEquals(1, sol.size());
	}
}
