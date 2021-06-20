/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.core.agent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;

/**
 * Controller for an {@link Agent}.
 *
 * The Controller starts a process that can be used by subclasses to drive the
 * agents actions. This process can be stopped, started and reset through the
 * agent.
 */
public abstract class Controller {
	private ExecutorService executor;
	/**
	 * The agent controlled by the controller.
	 */
	protected Agent<? extends Controller> agent;
	/**
	 * A throwable that can be set by an implementation of this class upon any
	 * unexpected error.
	 */
	protected Throwable throwable = null;
	/**
	 * DOC
	 */
	protected volatile boolean running = false;
	/**
	 * Flag that indicates that the agent has been terminated. That is, the agent
	 * has completely finished and all processes associated with running the agent
	 * have been shut down (terminated).
	 * <p>
	 * If true, this also implies that the agent is not running.
	 * </p>
	 * <p>
	 * If true, this does not necessarily mean that all resources that were used to
	 * run the agent have been released, nor that all necessary clean up has been
	 * performed.
	 * </p>
	 */
	private volatile boolean terminated = true;
	/**
	 * DOC
	 */
	private boolean disposeOnTermination = true;
	/**
	 *
	 */
	private volatile boolean myTurn = false;

	/**
	 * Initializes the controller with the agent it controls. Subclasses can
	 * override this method to do their own initialization against the agent.
	 * Subclasses should take care to call super method.
	 *
	 * @param agent    The agent that is controlled by this controller.
	 * @param executor The executor that should be used.
	 * @param timeout  A timestamp (in millisecond precision) at which the
	 *                 controller should be terminated; 0 means run indefinately.
	 * @throws GOALLaunchFailureException
	 */
	protected void initalizeController(Agent<? extends Controller> agent, ExecutorService executor, long timeout)
			throws GOALLaunchFailureException {
		this.agent = agent;
		this.executor = executor;
	}

	/**
	 * @return any uncaught throwable caught during the execution of the agent.
	 */
	public final Throwable getUncaughtThrowable() {
		return this.throwable;
	}

	/**
	 * The agent can be disposed after its process terminates. This releases any
	 * resources held by the agent, which can increase performance in systems with
	 * many ephemeral agents.
	 * <p>
	 * By default the agent is disposed; this function disables the disposing upon
	 * termination.
	 * </p>
	 */
	public final void keepDataOnTermination() {
		this.disposeOnTermination = false;
	}

	/**
	 * @return {@code true} if the agent is running.
	 */
	public final boolean isRunning() {
		return this.running;
	}

	/**
	 * @return {@code true} if the agent has completely finished running (cleaned-up
	 *         all its resources and such).
	 */
	public final boolean isTerminated() {
		return this.terminated;
	}

	/**
	 * Starts the agent. Only external classes should call this.
	 */
	public final void run() {
		if (!this.running) {
			this.running = true;
			this.terminated = false;
			this.executor.execute(getRunnable(this.executor, null));
		}
	}

	public final void giveTurn() {
		this.myTurn = true;
	}

	public final boolean hasTurn() {
		return this.myTurn;
	}

	public final void endTurn() {
		this.myTurn = false;
	}

	/**
	 * Stops the agent. Only external classes should call this. Currently, the
	 * termination is not immediate, but it prevents an agent from submitting its
	 * next task to the executor service, after which it cleans up itself. Use
	 * awaitTermination to ensure the agent has really terminated itself.
	 */
	public final void terminate() {
		if (this.running) {
			this.running = false;
			onTerminate();
		}
	}

	/**
	 * This function can be implemented by clients; it does nothing by default. It
	 * is run upon a call to terminate.
	 */
	protected void onTerminate() {
	}

	/**
	 * Flag other processes that we are really done running, and dispose our
	 * resources when disposeOnTermination is true. An implementing class should
	 * always call this: it sees running=false upon which it cleans up itself and
	 * then calls this function afterwards.
	 *
	 * @throws InterruptedException
	 */
	protected final void setTerminated() throws InterruptedException {
		if (!this.terminated) {
			terminate(); // just to be sure
			synchronized (this) {
				this.terminated = true;
				notifyAll();
				if (this.disposeOnTermination) {
					dispose();
				}
			}
		}
	}

	/**
	 * Resets the agent. To reset the agent, it is first stopped and then started
	 * again.
	 *
	 * @throws InterruptedException when interrupted while waiting for the agent to
	 *                              stop.
	 */
	public final void reset() throws InterruptedException {
		terminate();
		awaitTermination();
		onReset();
		run();
	}

	/**
	 * This function can be implemented by clients; it does nothing by default. It
	 * is run upon a call to reset, right after an agent is stopped and before it
	 * runs again, to allow external resources to reset as well.
	 */
	protected abstract void onReset();

	/**
	 * Waits until the agent has terminated.
	 *
	 * @throws InterruptedException If another thread interrupts this method.
	 */
	public final void awaitTermination() throws InterruptedException {
		synchronized (this) {
			while (!this.terminated) {
				wait();
			}
		}
	}

	/**
	 * Called automatically upon stop with disposeOnTermination set to true.
	 */
	public void dispose() {
		this.executor.shutdownNow();
	}

	/**
	 * Runs the agent by submitting tasks to the executor. Subclasses should
	 * implement their agents logic here. This logic should always check isRunning,
	 * and stop the logic when this becomes false, after which it can clean-up
	 * itself and should call setTerminated when it is really done. Care should be
	 * taken with regards to thread-safety and responsiveness (e.g. don't ignore
	 * interrupted exceptions).
	 *
	 * @param executor The executor to submit tasks to
	 * @param in       The task to run (initially null; the implementation should
	 *                 create the first task in this case)
	 *
	 * @return A runnable that implements all of the features mentioned in the
	 *         description.
	 */
	protected abstract Runnable getRunnable(final ExecutorService executor, final Callable<Callable<?>> in);

}