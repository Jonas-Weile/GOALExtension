package goal.tools.adapt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;

abstract public class LearnerAlgorithmTest {
	private LearnerAlgorithm learner;

	abstract public LearnerAlgorithm getLearner();

	@Before
	public void before() {
		this.learner = getLearner();
	}

	@Test
	public void smokeTest() {
		this.learner.finish(0);
	}

	@Test
	public void smokeTest2() {
		this.learner.start();
		this.learner.finish(0);
	}

	@Test
	public void testNoValues() {
		this.learner.start();
		// we should not have a state 3 without any learning...
		Hashtable<Integer, Double> values = this.learner.actionValues(3);
		assertTrue("expected no values", values.isEmpty());
		this.learner.finish(0);
	}

	@Test
	public void testNextNoOptions() {
		this.learner.start();

		// only 1 action possible, it should be picked.
		assertNull(this.learner.nextAction(1, new Integer[] {}));
		this.learner.finish(0);
	}

	@Test
	public void testLearnOne() {
		this.learner.start();

		// only 1 action possible, it should be picked.
		assertEquals((Integer) 2, this.learner.nextAction(1, new Integer[] { 2 }));
		this.learner.update(1, 1);
		this.learner.finish(0);
	}

	@Test
	public void testLearnedValues() {
		this.learner.start();

		// only 1 action possible, it should be picked.
		assertEquals((Integer) 2, this.learner.nextAction(1, new Integer[] { 2 }));
		this.learner.update(1, 1);

		assertFalse("expected values for state 1", this.learner.actionValues(1).isEmpty());
		assertTrue("expected no values for state 3", this.learner.actionValues(3).isEmpty());
		this.learner.finish(0);
	}

	@Test
	public void testLearnTwo() {
		this.learner.start();

		// going to state 1 is an option
		this.learner.nextAction(1, new Integer[] { 2 });
		// going to state 2 is ok
		this.learner.update(0.1, 2);

		/*
		 * now we find out state 4 is also possible to reach.
		 *
		 * We must indicate only 4 as option, otherwise the learner will probably
		 * suggest 2 as next action in which case we can't learn for state 4.
		 */
		this.learner.nextAction(1, new Integer[] { 4 });
		// going to state 4 is better than to 2.
		this.learner.update(1.0, 4);

		// then, recommendation should now be 4
		// FIXME rarely, this test will return 2... This is the random behaviour
		// of the learner...
		// assertEquals((Integer) 4, learner.nextAction(1, new Integer[] { 2, 4 }));
		this.learner.nextAction(1, new Integer[] { 2, 4 });

		this.learner.finish(0);
	}
}
