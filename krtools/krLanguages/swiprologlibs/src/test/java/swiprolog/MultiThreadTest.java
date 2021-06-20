package swiprolog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jpl7.Query;
import org.jpl7.Term;
import org.junit.Test;

/**
 * Test if multiple swi engines can be started in parallel
 *
 * @author W.Pasman 9may16
 */
public class MultiThreadTest {
	private static final int NTHREADS = 88;

	static {
		SwiInstaller.init();
	}

	@Test
	public void singleThreadTest() throws InterruptedException {
		System.out.println("Single-thread test");
		// we do not clear the SWI database. Make sure we do not reuse an
		// existing module.
		testSimpleQueries(9999);

	}

	// @Test
	public void multiThreadTest() throws InterruptedException {
		System.out.println("Multi-thread test with simple query");
		List<Thread> threads = new ArrayList<>();
		for (int n = 0; n < 2000; n++) {
			threads.add(runSimpleThread(n));
		}
		while (!threads.isEmpty()) {
			Thread thread = threads.get(0);
			// System.out.println("Waiting for " + thread);
			thread.join();
			threads.remove(thread);
		}
	}

	private Thread runSimpleThread(final int n) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				new Query("between(1,1000,I),fail").allSolutions();
			}
		});
		thread.start();
		return thread;
	}

	// @Test
	public void multiThreadTestFibonnaci() throws InterruptedException {
		System.out.println("Multi-thread test fibonacci");
		List<Thread> threads = new ArrayList<>();
		for (int n = 0; n < NTHREADS; n++) {
			threads.add(runThreadFibonnaci(n));
		}
		while (!threads.isEmpty()) {
			Thread thread = threads.get(0);
			// System.out.println("Waiting for " + thread);
			thread.join();
			threads.remove(thread);
		}
	}

	// @Test
	public void multiThreadTestInsertDelete() throws InterruptedException {
		System.out.println("Multi-thread test insert/delete");
		List<Thread> threads = new ArrayList<>();
		for (int n = 0; n < NTHREADS; n++) {
			threads.add(runThreadInsertDelete(n));
		}
		while (!threads.isEmpty()) {
			Thread thread = threads.get(0);
			// System.out.println("Waiting for " + thread);
			thread.join();
			threads.remove(thread);
		}
	}

	private Thread runThreadFibonnaci(final int n) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					testSimpleQueries(n);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		return thread;
	}

	private void testSimpleQueries(int n) throws InterruptedException {
		String module = "robot_" + n;
		String formula = "test :- member(3,[1,2,3,4,5])";
		insert(module, formula);
		query(module, "test");

		insert(module, "fibo(0,0)");
		insert(module, "fibo(1,0)");
		insert(module, "fibo(X,Y):-X>1, X1 is X-1, X2 is X-2, fibo(X1,Y1), fibo(X2,Y2), Y is Y1 + Y2");
		// query(module, "listing");

		query(module, "fibo(22,Y)");
	}

	private Thread runThreadInsertDelete(final int n) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					testInsertDelete(n);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		return thread;
	}

	private void testInsertDelete(int n) throws InterruptedException {
		long endTime = System.currentTimeMillis() + 5000;

		while (System.currentTimeMillis() < endTime) {
			double random = Math.random();
			new Query("assert(r(" + random + "))").allSolutions();
			if (new Query("aggregate_all(count,r(_),Amount)").allSolutions().length < 1) {
				System.err.println("WARNING. Expected r to hold after insert");
			}
			new Query("retract(r(" + random + "))").allSolutions();
		}
	}

	private void insert(String module, String formula) {
		String f = "assert(" + module + ":(" + formula + "))";
		// System.out.println(f);
		new Query(f).hasSolution();
	}

	private Map<String, Term> query(String module, String formula) {
		String q = module + ":" + formula;
		// System.out.println(q);
		return new Query(q).allSolutions()[0];
	}
}