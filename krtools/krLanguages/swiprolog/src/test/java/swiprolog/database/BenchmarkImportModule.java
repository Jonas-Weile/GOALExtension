package swiprolog.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import swiprolog.SwiPrologInterface;

/**
 * Benchmark speed of importing/cloning a (knowledge) module into some other
 * module.
 */
@RunWith(Parameterized.class)
public class BenchmarkImportModule {
	private final String KB = "kb";
	private SwiPrologInterface swi;
	private final int NTESTS = 100;
	private final List<Term> kbterms = new ArrayList<>();
	private long start;
	private final String prologfile;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "/prolog/starcraft.pl" }, { "/prolog/tictactoe.pl" } });
	}

	public BenchmarkImportModule(final String file) {
		this.prologfile = file;
	}

	@Before
	@SuppressWarnings("deprecation")
	public void setUp() throws Exception {
		this.swi = new SwiPrologInterface();
		System.out.println("Testing " + this.prologfile);
		final Scanner scanner = new Scanner(getClass().getResourceAsStream(this.prologfile), "UTF-8");
		scanner.useDelimiter("\\A");
		final String text = scanner.next();
		scanner.close();

		for (final String know : text.split("\\.")) {
			this.kbterms.add(Util.textToTerm(know));
		}
		insertAllKnowledge(this.KB);
	}

	@After
	public void tearDown() throws Exception {
		this.swi.release();
	}

	@Test
	public void testKB() {
		query(this.KB, "lastitem");
	}

	@Test
	public void benchmarkAddImportModule() {
		start();
		for (int n = 0; n < this.NTESTS; n++) {
			makeBeliefbaseAddImportModule("bb" + n);
		}
		end("benchmarkAddImportModule");
	}

	/**
	 * For some reason, this test crashes the JVM! Might be
	 * https://github.com/SWI-Prolog/issues/issues/69.
	 */
	@Ignore
	@Test
	public void benchmarkSetBaseModule() {
		start();
		for (int n = 0; n < this.NTESTS; n++) {
			makeUsingSetBaseModule("bb" + n);
		}
		end("benchmarkSetBaseModule");
	}

	@Test
	public void benchmarkAssertAllKnowledge() {
		start();
		for (int n = 0; n < this.NTESTS; n++) {
			insertAllKnowledge("bb" + n);
		}
		end("benchmarkAssertAllKnowledge");
	}

	/*********************** support functions *****************/
	private void start() {
		this.start = System.nanoTime();

	}

	private void end(final String string) {
		final long end = System.nanoTime();
		final double time = (end - this.start) / this.NTESTS; // ns
		System.out.println(string + " took on average " + time / 1000000 + "ms per database creation.");
	}

	/**
	 * Create new module with given name and "insert" knowledge by using
	 * "add_import_module".
	 *
	 * @param base the module name to create
	 */
	private void makeBeliefbaseAddImportModule(final String base) {
		query(base, "assert(p)"); // create the new module
		query(base, "add_import_module(" + this.KB + "," + base + ",start)");
	}

	/**
	 * Create new module with given name and "insert" knowledge by using
	 * "set_base_module".
	 *
	 * @param base the module name to create
	 */
	private void makeUsingSetBaseModule(final String base) {
		query(base, "assert(p)"); // create the new module
		query(base, "set_base_module(" + this.KB + ")");
	}

	/**
	 * Create new module with name "bb<n>" and "insert" knowledge by inserting all
	 * knowledge one by one.
	 *
	 * @param n the number of the module
	 */

	private void insertAllKnowledge(final String module) {
		for (final Term knowledge : this.kbterms) {
			insert(module, knowledge);
		}
	}

	/**
	 * Insert knowledge in database
	 *
	 * @param know
	 */
	private void insert(final String module, final Term know) {
		final Compound queryterm = new Compound("assert",
				new Term[] { new Compound(":", new Term[] { new Atom(module), know }) });
		query(new Query(queryterm));
	}

	/**
	 * Do a query and check the result is OK.
	 *
	 * @param query the string to query
	 */
	private Map<String, Term>[] query(final String module, final String query) {
		return query(new Query(module + ":" + query));
	}

	/**
	 * Do a query and check the result is OK.
	 *
	 * @param query the string to query
	 */
	private Map<String, Term>[] query(final Query query) {
		Map<String, Term>[] res;
		try {
			res = query.allSolutions();
		} catch (final PrologException e) {
			throw new IllegalStateException("knowledge query " + query + " failed: ", e);
		}
		if (res.length == 0) {
			throw new IllegalStateException("knowledge query " + query + " returns false");
		}
		return res;
	}
}
