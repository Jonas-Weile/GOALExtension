package goal.tools.test.executors;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import events.Channel;
import events.ExecutionEventListener;
import goal.core.runtime.service.agent.RunState;
import goal.tools.debugger.DebugEvent;
import goal.tools.test.executors.testcondition.TestConditionExecutor;
import krTools.parser.SourceInfo;

public abstract class TestObserver extends ExecutionEventListener { // DebugObserver
	private Set<TestConditionExecutor> executors;

	protected void initialize(RunState runstate) {
		this.executors = new LinkedHashSet<>();
		runstate.getEventGenerator().addListener(this);
	}

	public void add(TestConditionExecutor executor) {
		this.executors.add(executor);
	}

	public void remove(TestConditionExecutor executor) {
		this.executors.remove(executor);
	}

	public TestConditionExecutor[] getExecutors() {
		return this.executors.toArray(new TestConditionExecutor[this.executors.size()]);
	}

	/**
	 * The channels that we listen to. Module entries might also add a set of
	 * beliefs/goals. Note: event module entry handles percept/mails as well Module
	 * exits might drop goals. Note: main module exit means agent exit
	 */
	List<Channel> channels = Arrays
			.asList(new Channel[] { Channel.ACTION_EXECUTED_BUILTIN, Channel.ACTION_EXECUTED_MESSAGING,
					Channel.ACTION_EXECUTED_USERSPEC, Channel.MODULE_ENTRY, Channel.MODULE_EXIT });

	@Override
	public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		if (!this.channels.contains(channel)) {
			return;
		}
		for (TestConditionExecutor executor : getExecutors()) {
			// FIXME Bit of a hack to reconnect the evaluate function
			// maybe we can do without the DebugEvent here?
			executor.evaluate(new DebugEvent(null, null, channel, associateObject, associateSource, message, args));
		}
	}
}
