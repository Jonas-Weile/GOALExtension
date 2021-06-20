package goal.tools.profiler;

/**
 * Accumulates statistics: accumulated time and number of calls to some object
 */
public class Statistic {

	/**
	 * The accumulated sum of times(ns) added to this statistic. We store long
	 * (and not e.g. float or double) to avoid rounding issues when accumulating
	 * small numbers to large numbers, eg when calculating total used CPU time.
	 */
	private long totalTime = 0;
	/**
	 * Number of values that have been added to this statistic.
	 */
	private long number = 0;

	public Statistic() {
	}

	/**
	 * 
	 * @param totalTime
	 *            The accumulated sum of times(ns) added to this statistic. We
	 *            store long (and not e.g. float or double) to avoid rounding
	 *            issues when accumulating small numbers to large numbers, eg
	 *            when calculating total used CPU time
	 * @param number
	 *            Number of values that have been added to this statistic
	 */
	public Statistic(long totalTime, long number) {
		this.totalTime = totalTime;
		this.number = number;
	}

	/**
	 * Add one call with the given time (nano seconds) to this stat.
	 *
	 * @param time
	 *            (nanoseconds) to add to this stat
	 */
	public void add(long time) {
		this.totalTime += time;
		this.number++;
	}

	/*
	 * @return number of calls made
	 */
	public long getTotalNumber() {
		return this.number;
	}

	/**
	 * @return accumulated nanoseconds spent
	 */
	public long getTotalTime() {
		return this.totalTime;
	}

}
