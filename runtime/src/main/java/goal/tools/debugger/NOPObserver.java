package goal.tools.debugger;

import events.Channel;

public class NOPObserver implements DebugObserver {
	private final ObservableDebugger debugger;

	public NOPObserver(ObservableDebugger debugger) {
		this.debugger = debugger;
	}

	public void subscribe() {
		this.debugger.subscribe(this, Channel.NONE);
	}

	@Override
	public String getObserverName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean notifyBreakpointHit(DebugEvent event) {
		return true;
	}
}
