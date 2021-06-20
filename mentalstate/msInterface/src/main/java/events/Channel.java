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
package events;

/**
 * Represents different event channels on which ExecutionEvents can be posted.
 */
public enum Channel {
	/**
	 * Special channel for the separator between reasoning cycles.
	 */
	REASONING_CYCLE_SEPARATOR("The reasoning cycle separator", Integer.MAX_VALUE),

	/**
	 * Special channel for print actions from agent.
	 */
	PRINT("Print statements", Integer.MAX_VALUE),

	/**
	 * Channel for reports on received mails.
	 */
	MAILS("Mails received", Integer.MAX_VALUE),

	/**
	 * Channel for reporting percepts inserted into percept base, but only if
	 * PERCEPTS channel has VIEW state.
	 */
	MAILS_CONDITIONAL_VIEW("Changes to the mailbox", Integer.MAX_VALUE),

	/**
	 * Channel for reports on received percepts.
	 */
	PERCEPTS("Percepts processed", Integer.MAX_VALUE),

	/**
	 * Channel for reporting percepts inserted into percept base, but only if
	 * PERCEPTS channel has VIEW state.
	 */
	PERCEPTS_CONDITIONAL_VIEW("Changes to the percept base", Integer.MAX_VALUE),

	/**
	 * Channel for reporting on the entry of a module.
	 */
	MODULE_ENTRY("Entry of a module", 1),
	/**
	 * Channel for reporting on the exit of a module.
	 */
	MODULE_EXIT("Exit of a module", Integer.MAX_VALUE),

	/**
	 * Channel for reporting the call (before prepost or entry) of any action or
	 * module.
	 */
	CALL_ACTION_OR_MODULE("Call to an action or module", 3),

	/**
	 * Channel for reporting on the evaluation of a rule's condition.
	 */
	RULE_CONDITION_EVALUATION("Evaluation of rule conditions", 2),

	/**
	 * Channel for reporting on the evaluation of a rule's condition. Passes a
	 * different object, used for conditional breakpoints!
	 */
	HIDDEN_RULE_CONDITION_EVALUATION("Evaluation of rule conditions (for breakpoints)", Integer.MAX_VALUE),

	/**
	 * Channel for reporting that a rule is being evaluated
	 */
	RULE_CONDITIONAL_VIEW("Starting evaluation of rule", Integer.MAX_VALUE),

	/**
	 * Channel for reports on actions going to be executed.
	 */
	ACTION_PRECOND_EVALUATION("Evaluation of action pre-conditions", 4),

	/**
	 * Channel for reports on actions going to be executed.
	 */
	ACTION_POSTCOND_EVALUATION("Evaluation of action post-conditions", 4),

	/**
	 * Channel for reports on built-in actions that have been executed.
	 */
	ACTION_EXECUTED_BUILTIN("Built-in actions that have been executed", Integer.MAX_VALUE),

	/**
	 * Channel for reports on messaging actions that have been executed.
	 */
	ACTION_EXECUTED_MESSAGING("Messaging actions that have been executed", Integer.MAX_VALUE),

	/**
	 * Channel for reports on user-spec actions that have been executed.
	 */
	ACTION_EXECUTED_USERSPEC("User-specified actions that have been executed", Integer.MAX_VALUE),

	/**
	 * Channel for reports on additions to / deletions from the belief base.
	 */
	BB_UPDATES("Changes to the belief base", Integer.MAX_VALUE),

	/**
	 * Channel for reports on additions to / deletions from the goal base.
	 */
	GB_UPDATES("Changes to the goal base", Integer.MAX_VALUE),

	/**
	 * Channel for reports on any changes to the goal base. This is a channel
	 * similar to {@link #GB_UPDATES} but hidden.
	 */
	GB_CHANGES("Changes to the goal base", Integer.MAX_VALUE),

	/**
	 * Channel for reports on goals that have been achieved (and not dropped).
	 * TODO: cycle breakpoint
	 */
	GOAL_ACHIEVED("Goals that have been achieved", 0),

	/**
	 * Channel to report on changes in run mode. Internal use only.
	 */
	RUNMODE("Run mode changes of agent", Integer.MAX_VALUE),

	/**
	 * Channel to report clearing the agent's mental state.
	 */
	CLEARSTATE("Clearing the mental state of agent", Integer.MAX_VALUE),

	/**
	 * Channel to report that agent has gone to sleep.
	 */
	SLEEP("Going to sleep or waking up", Integer.MAX_VALUE),

	/**
	 * Special channel for notifying the debugger for user-defined breakpoints.
	 */
	BREAKPOINTS("User-defined breakpoints", 0),

	/**
	 * Special channel for notifying the debugger for user-defined breakpoints.
	 */
	TESTFAILURE("Test failure", 0),

	/**
	 * Special channel for nothing
	 */
	NONE("None", Integer.MAX_VALUE),
	/**
	 * a database query is starting.
	 */
	DB_QUERY_START("query", Integer.MAX_VALUE),
	/**
	 * a database query is completed.
	 */
	DB_QUERY_END("completed query", Integer.MAX_VALUE),

	/**
	 * a insert is starting.
	 */
	INSERT_START("insert", Integer.MAX_VALUE),
	/**
	 * a insert is completed.
	 */
	INSERT_END("inserted", Integer.MAX_VALUE),

	/**
	 * a insert is starting.
	 */
	DELETE_START("delete", Integer.MAX_VALUE),
	/**
	 * a insert is completed.
	 */
	DELETE_END("deleted", Integer.MAX_VALUE),

	/**
	 * a insert is starting.
	 */
	ADOPT_START("adopt", Integer.MAX_VALUE),
	/**
	 * a insert is completed.
	 */
	ADOPT_END("adopted", Integer.MAX_VALUE),

	/**
	 * a drop is starting.
	 */
	DROP_START("drop", Integer.MAX_VALUE),
	/**
	 * a drop is completed.
	 */
	DROP_END("dropped", Integer.MAX_VALUE),
	/**
	 * mental state query is started. A mentalstate query can contain multiple
	 * {@link #DB_QUERY_START}
	 */
	MSQUERY_START("ms query", Integer.MAX_VALUE),
	/**
	 * mental state query is ended
	 */
	MSQUERY_END("ms queried", Integer.MAX_VALUE),

	/**
	 * Actioncombo start/pushed
	 */
	ACTIONCOMBO_START("actioncombo start", Integer.MAX_VALUE),

	/**
	 * Actioncombo end
	 */
	ACTIONCOMBO_END("actioncombo end", Integer.MAX_VALUE),

	/**
	 * Start of Action executor. Notice, each action may take multiple or zero
	 * cycles to completion. In the end, no action at all may be executed. So do
	 * not use this to count the number of actions executed.
	 *
	 */
	ACTION_START("execute action", Integer.MAX_VALUE),
	/**
	 * Action end
	 */
	ACTION_END("executed action", Integer.MAX_VALUE),

	/**
	 * Starting rule evaluation and execution.
	 */
	RULE_START("rule start", Integer.MAX_VALUE),

	/**
	 * Ended rule evaluation
	 */
	RULE_EVAL_CONDITION_DONE("rule evaluated condition", Integer.MAX_VALUE),

	/**
	 * rule completed execution.
	 */
	RULE_EXIT("rule completed execution", Integer.MAX_VALUE),
	/**
	 * The warning channel contains exceptions that occured during execution but
	 * were ignored/recovered. The warnings should have a human readable error
	 * message. A stacktrace should be added too, if it can be relevant for the
	 * user (e.g. when it points to a bug in the environment or other
	 * goal-external code)
	 */
	WARNING("recoverable exceptions while executing the agent", Integer.MAX_VALUE);

	/**
	 * The stepping level of the Channel.
	 */
	private final int level;
	/**
	 * Text string used to explain channel in debug preference pane.
	 */
	private final String explanation;

	/**
	 * FIXME Channels contain information that is exclusively for the Debugger
	 * and should not be here #3732.
	 *
	 * @param explanation
	 * @param level
	 */
	private Channel(String explanation, int level) {
		this.explanation = explanation;
		this.level = level;
	}

	/**
	 * Return explanation text for channel.
	 *
	 * @return text that explains function of channel.
	 */
	public String getExplanation() {
		return this.explanation;
	}

	/**
	 * @return The stepping level of this {@link Channel}.
	 */
	public int getLevel() {
		return this.level;
	}

	public static Channel getConditionalChannel(Channel channel) {
		switch (channel) {
		case PERCEPTS:
			return PERCEPTS_CONDITIONAL_VIEW;
		case RULE_CONDITION_EVALUATION:
			return RULE_CONDITIONAL_VIEW;
		case ACTION_PRECOND_EVALUATION:
			return RULE_CONDITIONAL_VIEW;
		default:
			// return channel itself it is does not have a related condition
			// channel.
			return channel;
		}
	}

	/**
	 * The state of a Channel.
	 * <p>
	 * It seems that this is mainly a mix of (1) initial state (both for PAUSING
	 * and for VIEW column) for the channel in the breakpoint preferences panel
	 * (2) whether the channel is visible at all in that panel.
	 */
	public enum ChannelState {
		/**
		 * Hidden channels will never be displayed to the user (in the debug
		 * preference panel). They only serve as internal event notifications.
		 */
		HIDDEN("Internal"),
		/**
		 * Same as {@link #HIDDEN}, but the debugger will always pause on the
		 * channel. Useful for breakpoints.
		 */
		HIDDENPAUSE("Internal Pause"),
		/**
		 * Same as {@link #HIDDEN}, but the debugger will always display debug
		 * messages on the channel.
		 */
		HIDDENVIEW("Internal Display"),
		/**
		 * Same as {@link #HIDDEN}, but the debugger may present debug messages
		 * on the channel to the user.
		 */
		CONDITIONALVIEW("Internal Conditional"),
		/**
		 * Channels in the NONE state will not be displayed in the debug tracer,
		 * and will not be paused upon when stepping.
		 */
		NONE("Don't log or break"),
		/**
		 * Channels in the VIEW state will be displayed in the debug tracer, but
		 * will not be paused upon when stepping.
		 */
		VIEW("Log"),
		/**
		 * Channels in the PAUSE state will be paused upon when stepping.
		 */
		PAUSE("Break"),
		/**
		 * Channels in the VIEWPAUSE state will be displayed in the debug
		 * tracer, and will also be paused upon when stepping.
		 */
		VIEWPAUSE("Log and break");

		/**
		 * text string used to explain channelstate in debug preference pane.
		 */
		private String explanation;

		ChannelState(String explanation) {
			this.explanation = explanation;
		}

		/**
		 * Used to hide channels in breakpoint preference pane.
		 *
		 * @return {@code true} if this is a hidden channel state.
		 */
		public boolean isHidden() {
			return this == HIDDEN || this == HIDDENPAUSE || this == HIDDENVIEW || this == CONDITIONALVIEW;
		}

		/**
		 * @return {@code true} if the user should see this in the debug tracer.
		 */
		public boolean canView() {
			return this == VIEW || this == VIEWPAUSE || this == HIDDENVIEW;
		}

		/**
		 * @return {@code true} if we should step on this channel state.
		 */
		public boolean shouldPause() {
			return this == PAUSE || this == VIEWPAUSE || this == HIDDENPAUSE;
		}

		/**
		 * Return explanation text for channelstate.
		 *
		 * @return text that explains function of channelstate.
		 */
		public String getExplanation() {
			return this.explanation;
		}
	}
}
