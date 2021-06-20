package goal.core.runtime.service.agent;

import java.lang.management.ManagementFactory;

/**
 * Class that supports actual CPU run time for the agent. Thread safe.
 * <h1>Why use this?</h1> The usual approach with: <code>
 * ManagementFactory.getThreadMXBean().getThreadCpuTime(
				Thread.currentThread().getId()); 
				</code> is not working because the interpreter is running agents
 * in various threads. This class keeps track of the last used thread.
 */
public class NettoRunTime {
	/**
	 * The thread time when we started running on this thread. 0 means not
	 * initialized.
	 */
	private volatile long threadStartTime = 0;
	/**
	 * accumulated run time from earlier threads than the one we're on right now
	 */
	private volatile long fromPreviousThreads = 0;
	/**
	 * The thread we are running on. null means not initialized.
	 */
	private volatile Thread currentThread = null;
	/**
	 * The last current timestamp we took
	 */
	private volatile long now = 0;

	public NettoRunTime() {
		getCurrentThread();
	}

	/**
	 * @return the actual CPU time (netto run time received).
	 */
	public long get() {
		this.now = ManagementFactory.getThreadMXBean().getThreadCpuTime(getCurrentThread().getId());
		return this.fromPreviousThreads + this.now - this.threadStartTime;
	}

	/**
	 * This function should be called when a thread is left. The reason we need this
	 * is that there may be other users of the thread after you know you leave it.
	 * If you happen to be on the same thread again when you return and you do not
	 * call this, your thread may be charged with CPU used by other users of the
	 * thread.
	 */
	public void leaveThread() {
		if (this.currentThread != null) {
			// we have been initialized and thus have been running before
			// remember time we ran on previous thread.
			this.fromPreviousThreads += this.now - this.threadStartTime;
			this.currentThread = null;
		}
	}

	/**
	 * get current thread. If changed, recalibrate timers.
	 *
	 * @param thread the new thread we are now on.
	 */
	private Thread getCurrentThread() {
		Thread thread = Thread.currentThread();
		if (thread == this.currentThread) {
			return thread;
		}

		if (this.currentThread != null) {
			System.err.println("WARNING: some calls did not complete normally. Their CPU time was not yet accumulated");
			leaveThread();
		}
		this.currentThread = thread;
		this.threadStartTime = ManagementFactory.getThreadMXBean().getThreadCpuTime(this.currentThread.getId());
		return thread;
	}
}
