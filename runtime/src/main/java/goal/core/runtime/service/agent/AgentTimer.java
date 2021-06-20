package goal.core.runtime.service.agent;

import java.util.concurrent.TimeUnit;

public class AgentTimer {
	private final String name;
	private final long interval;
	private final long startTime;
	private final long endTime;

	/**
	 * @param name
	 *            The (unique) name of the timer.
	 * @param interval
	 *            The timer's interval (ms).
	 * @param duration
	 *            The timer's duration (ms).
	 */
	public AgentTimer(String name, long interval, long duration) {
		this.name = name;
		this.interval = interval;
		this.startTime = System.nanoTime();
		this.endTime = this.startTime + TimeUnit.MILLISECONDS.toNanos(duration);
	}

	/**
	 * @return The (unique) name of the timer.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return How many milliseconds elapsed since starting the timer, rounded to
	 *         the nearest multiple of the given interval.
	 */
	public long getElapsed() {
		long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.startTime);
		return this.interval * Math.round(elapsed / this.interval);
	}

	/**
	 * @return True iff the timer has ran for at least its duration.
	 */
	public boolean hasFinished() {
		return (System.nanoTime() >= this.endTime);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AgentTimer) {
			AgentTimer other = (AgentTimer) obj;
			return this.name.equals(other.name);
		} else {
			return false;
		}
	}
}
