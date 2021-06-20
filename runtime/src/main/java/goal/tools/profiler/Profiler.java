package goal.tools.profiler;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;

import events.Channel;
import events.ExecutionEventGenerator;
import events.ExecutionEventListener;
import goal.core.agent.Agent;
import goal.core.executors.stack.CallStack;
import goal.core.executors.stack.StackExecutor;
import goal.core.runtime.service.agent.NettoRunTime;
import goal.core.runtime.service.agent.RunState;
import goal.util.datatable.DataRow;
import krTools.parser.SourceInfo;
import languageTools.program.agent.rules.Rule;

/**
 * profiler that counts (for one {@link Agent}) the number of calls to and time
 * spent in modules, rules, etc. The time spent is the bruto time between the
 * entry and the exit of the module. So that means that it includes time spent
 * in rules, action combo executors, etc.
 * <p>
 * This profiler computes the actual CPU time spent in the objects, not the wall
 * clock time. This avoids charging the objects for waiting time.
 * <p>
 * To use this, subscribe a profiler to the {@link ExecutionEventGenerator} in
 * the {@link RunState}.
 */
public class Profiler extends ExecutionEventListener {
	/**
	 * statistics of the profiled objects. The keys are the associateObjects
	 * coming from the core, or a carefully crafted object for a number of
	 * global accumulated properties (see the constants below). Proper
	 * implementation of these objects equals() function is essential so that
	 * events originating from the same Object map into the same
	 * ProfileStatistic.
	 */
	// private final Map<Object, ProfileStatistic> objectStatistics = new
	// LinkedHashMap<>();
	AgentProfile objectStatistics;
	/**
	 * A stack of calls made to us. This is in fact a copy of the
	 * {@link CallStack},but maintained by events coming from the core.
	 */
	private final Deque<ObjectInfo> stack = new LinkedList<>();
	/**
	 * netto run time for the agent.
	 */
	private final NettoRunTime time;
	/**
	 * objects representing database actions. We use these instead of the actual
	 * actions, as we want to aggregate these calls. The string contents are in
	 * fact never used
	 */
	private static final String DB_QUERY = "KR Query";
	private static final String INSERT = "KR Insert";
	private static final String DELETE = "KR Delete";
	private static final String ADOPT = "KR Adopt";
	private static final String DROP = "KR Drop";
	private static final String MS_COND = "Mental State Condition";

	private long cycleStart;

	/**
	 * 
	 * @param t
	 *            the {@link NettoRunTime}
	 * @param agentTypeName
	 *            is the agent name as specified in the MAS file. Here, the name
	 *            is a generic type and there may be multiple agents running of
	 *            the same type. Profiles with the same agentTypeName are
	 *            eligible for data accumulation after the run.
	 */
	public Profiler(NettoRunTime t, String agentTypeName) {
		this.time = t;
		objectStatistics = new AgentProfile(agentTypeName);
	}

	/**
	 * An object was just started to run. Record the start time.
	 *
	 * @param object
	 *            the associateObject (typically, an {@link StackExecutor} that
	 *            just started
	 * @param info
	 * @param type
	 *            the type if the info we have here.
	 */
	private void start(Object associateObject, SourceInfo info, InfoType type) {
		Object parent = null;
		if (!this.stack.isEmpty()) {
			parent = this.stack.getFirst().getCaller();
		}

		// create the Statistics if necessary.
		ProfileStatistic stat = this.objectStatistics.get(associateObject);

		if (stat == null) {
			stat = new ProfileStatistic(info, type, getName(associateObject), this.objectStatistics.get(parent),
					associateObject);
			this.objectStatistics.put(stat);
		}

		this.stack.push(new ObjectInfo(associateObject, this.time.get(), info, type, stat));
	}

	/**
	 * Similar to {@link #start(Object, SourceInfo, InfoType)} but for KR calls.
	 * KR calls are special: they need a de-referencing of associateObject AND
	 * they need global accumulation.
	 *
	 * @param dereferenceLabel
	 *            the label to use to de-reference the associateSource
	 *
	 * @param info
	 */
	private void startKrCall(String dereferenceLabel, SourceInfo info) {
		start(ref(dereferenceLabel, info), info, InfoType.KR_CALL);
	}

	/**
	 * An object that just stopped running. Add the time since start in the
	 * {@link #objectStatistics}. A stopped object should be on top of stack.
	 *
	 * @param object
	 *            the object (typically, an {@link StackExecutor} that just
	 *            started
	 * @return the time (ns) spent in the last call.
	 */

	private long end(Object associateObject) {
		ObjectInfo info = pop(associateObject);
		ProfileStatistic parent = null;
		if (!this.stack.isEmpty()) {
			parent = this.stack.getFirst().getStatistic();
		}
		long deltaT = this.time.get() - info.getStart();
		updateStats(associateObject, info.getSourceInfo(), info.getType(), deltaT, parent);
		return deltaT;
	}

	/**
	 * Similar to {@link #end(Object)} but for KR calls. KR calls are special:
	 * they need a de-referencing of associateObject AND they need global
	 * accumulation.
	 *
	 * @param dereferenceLabel
	 *            the label to use to de-reference the associateSource
	 * @param info
	 */
	private void endkrCall(String dereferenceLabel, SourceInfo info) {
		long deltaT = end(ref(dereferenceLabel, info));
		updateStats(dereferenceLabel, null, InfoType.GLOBAL, deltaT, null);
	}

	/**
	 * pop head of stack and match against AssociateObject
	 *
	 * @param associateObject
	 *            the expected object on top of stack
	 * @return ObjectInfo for top of stack.
	 */
	private ObjectInfo pop(Object associateObject) {
		ObjectInfo info = this.stack.pop();
		if (info.getCaller().equals(associateObject)) {
			return info;
		} else {
			// if we get here, the info coming from the core is not balanced
			throw new IllegalStateException("the object " + associateObject
					+ " that ends is not the object that started (" + info.getCaller() + ")");
		}
	}

	/**
	 * associateObject has performed an action that took deltaT time. Accumulate
	 * this into the statistics of associateObject.
	 *
	 * @param associateObject
	 *            the object that performed an action
	 * @param info
	 * @param type
	 * @param deltaT
	 *            the time spent by associateObject.
	 */
	private void updateStats(Object associateObject, SourceInfo info, InfoType type, Long deltaT,
			ProfileStatistic parent) {
		ProfileStatistic stat = this.objectStatistics.get(associateObject);
		if (stat == null) {
			// FIXME why can this happen? We should create them when they are
			// started
			stat = new ProfileStatistic(info, type, getName(associateObject), parent, associateObject);
			this.objectStatistics.put(stat);
		}
		stat.add(deltaT);
	}

	/**
	 *
	 * @param associateObject
	 * @return simplename string of associateObject. If associateObject is a
	 *         String, just the string, else the object's class name followed by
	 *         its toString representation.
	 */
	private String getName(Object associateObject) {
		String name = associateObject.toString();
		if (!associateObject.getClass().equals(String.class)) {
			name = associateObject.getClass().getSimpleName() + ":" + name;
		}
		return name;
	}

	@Override
	public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		switch (channel) {
		case REASONING_CYCLE_SEPARATOR:
			// cycle separator is not related to the stack. Handle separately.
			if (this.cycleStart != 0) {
				// associateObject is just an integer, cant use that
				updateStats("#completed rounds", associateSource, InfoType.GLOBAL, this.time.get() - this.cycleStart,
						null);
			}
			this.cycleStart = this.time.get();
			break;
		case MODULE_ENTRY:
			start(associateObject, associateSource, InfoType.MODULE);
			break;
		case MODULE_EXIT:
			end(associateObject);
			break;
		case RULE_START:
			// we start 2 counters, one for the entire rule and one for the
			// condition.
			start(associateObject, associateSource, InfoType.RULE);
			start(((Rule) associateObject).getCondition(), associateSource, InfoType.RULE_CONDITION);
			break;
		case RULE_EXIT:
			end(associateObject);
			break;
		case RULE_EVAL_CONDITION_DONE:
			end(((Rule) associateObject).getCondition());
			break;
		case ACTIONCOMBO_START:
			start(associateObject, associateSource, InfoType.RULE_ACTION);
			break;
		case ACTIONCOMBO_END:
			end(associateObject);
			break;
		case ACTION_START:
			start(associateObject, associateSource, InfoType.RULE_ACTION);
			break;
		case ACTION_END:
			end(associateObject);
			break;
		case DB_QUERY_START:
			startKrCall(DB_QUERY, associateSource);
			break;
		case DB_QUERY_END:
			endkrCall(DB_QUERY, associateSource);
			break;
		case INSERT_START:
			startKrCall(INSERT, associateSource);
			break;
		case INSERT_END:
			endkrCall(INSERT, associateSource);
			break;
		case DELETE_START:
			startKrCall(DELETE, associateSource);
			break;
		case DELETE_END:
			endkrCall(DELETE, associateSource);
			break;
		case ADOPT_START:
			startKrCall(ADOPT, associateSource);
			break;
		case ADOPT_END:
			endkrCall(ADOPT, associateSource);
			break;
		case DROP_START:
			startKrCall(DROP, associateSource);
			break;
		case DROP_END:
			endkrCall(DROP, associateSource);
			break;
		case MSQUERY_START:
			startKrCall(MS_COND, associateSource);
			break;
		case MSQUERY_END:
			endkrCall(MS_COND, associateSource);
			break;
		default:
			break;
		}
	}

	/**
	 * create a reference for an associate source. We need this for cases where
	 * the associateObject is too specific and we want to accumulate general
	 * calls to this source position, but we know we have a call that is
	 * specified by the given name
	 *
	 * @param name
	 *            the type of the call
	 * @param info
	 *            the source info for the call. If info==null, it is assumed the
	 *            source is the percept or mail module as these are known to
	 *            provide null source info.
	 * @return a tag that couples the name and source info.
	 */
	private String ref(String name, SourceInfo info) {
		String text;
		if (info == null) {
			text = "events";
		} else {
			text = info.toString();
		}
		return name + " " + text;
	}

	/**
	 * Informs us that the agent has been stopped and that we can stop the
	 * timer.
	 */
	public void stop() {
		ProfileStatistic runTime = new ProfileStatistic(null, InfoType.GLOBAL, "total run time", null,
				"total run time");
		runTime.add(this.time.get());
		objectStatistics.put(runTime);
	}

	public AgentProfile getProfile() {
		return objectStatistics;
	}

}

/**
 * info we store with each start
 */
class ObjectInfo {
	private Object caller; // the object that was associated with the start
	private Long startTime;
	private SourceInfo sourceInfo; // the source info associated with the
									// started object.
	private InfoType type;
	/**
	 * The statistic that handles this object.
	 */
	private ProfileStatistic statistic;

	public ObjectInfo(Object call, Long start, SourceInfo info, InfoType type, ProfileStatistic stat) {
		this.caller = call;
		this.startTime = start;
		this.sourceInfo = info;
		this.type = type;
		this.statistic = stat;
	}

	public Object getCaller() {
		return this.caller;
	}

	public Long getStart() {
		return this.startTime;
	}

	public SourceInfo getSourceInfo() {
		return this.sourceInfo;
	}

	public InfoType getType() {
		return this.type;
	}

	public ProfileStatistic getStatistic() {
		return this.statistic;
	}

}

/**
 * Rule to sort {@link ProfileStatistic}s based on 1. the {@link SourceInfo} 2.
 * The {@link InfoType} 3. the amount of time spent in each
 */
class CompareStats implements Comparator<DataRow> {
	@Override
	public int compare(DataRow r1, DataRow r2) {
		SourceInfo info1 = (SourceInfo) r1.column(ProfileStatistic.Column.INFO);
		SourceInfo info2 = (SourceInfo) r2.column(ProfileStatistic.Column.INFO);

		int compare;
		if (info1 == null) {
			if (info2 == null) {
				compare = 0;
			} else {
				compare = -1; // info1=null, info2!null
			}
		} else { // info1!null
			if (info2 == null) {
				compare = 1; // info1!null, info2=null
			} else {
				compare = info1.compareTo(info2);
			}
		}

		if (compare != 0) {
			return compare;
		}

		compare = ((InfoType) r1.column(ProfileStatistic.Column.TYPE))
				.compareTo((InfoType) r2.column(ProfileStatistic.Column.TYPE));
		if (compare != 0) {
			return compare;
		}

		// we sort time in decreasing order.
		return (int) Math.signum(
				(Double) r2.column(ProfileStatistic.Column.TIME) - (Double) r1.column(ProfileStatistic.Column.TIME));
	}

}
