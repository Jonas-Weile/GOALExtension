package goal.core.agent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * NOP Controller. Does not implement any of the functionality of a
 * {@link Controller} but can be used as a stub (e.g. in {@link AgentRegistry}).
 */
public class NOPController extends Controller {

	public NOPController() {
		this.running = true;
	}

	@Override
	protected void onReset() {
		// Does nothing.
	}

	@Override
	protected Runnable getRunnable(ExecutorService executor, Callable<Callable<?>> in) {
		return null;
	}

}