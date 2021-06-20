package org.eclipse.gdt.debug.dbgp;

import java.io.File;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import goal.tools.history.EventStorage;
import languageTools.program.agent.AgentId;

public class AgentState {
	private boolean initialized = false;
	private final AgentId agent;
	private final Set<String> beliefs;
	private final Set<String> goals;
	private final Set<String> percepts;
	private final Set<String> mails;
	private final Deque<String> focus;
	private final Deque<String> modules;
	private Set<String> condition;
	private EventStorage history;
	private String runmode;

	public AgentState(final AgentId agent) {
		this.agent = agent;
		this.beliefs = new LinkedHashSet<>();
		this.goals = new LinkedHashSet<>();
		this.percepts = new LinkedHashSet<>();
		this.mails = new LinkedHashSet<>();
		this.focus = new LinkedList<>();
		this.modules = new LinkedList<>();
		this.condition = new LinkedHashSet<>();
		this.runmode = "initializing";
	}

	public void reset() {
		this.beliefs.clear();
		this.goals.clear();
		this.percepts.clear();
		this.mails.clear();
		this.focus.clear();
		this.modules.clear();
		this.condition.clear();
	}

	public EventStorage getHistory(final LocalDebugger debugger) {
		if (this.history == null) {
			try {
				final File datafile = new File(debugger.getHistoryState(this.agent));
				this.history = new EventStorage(datafile);
			} catch (final Exception ignore) {
				this.history = null;
			}
		}
		return this.history;
	}

	public void setRunMode(final String mode) {
		this.initialized = true;
		this.runmode = mode;
	}
	
	public void deinitialize() {
		this.initialized = false;
	}

	public void addGoal(final String goal) {
		this.goals.add(goal);
	}

	public void removeGoal(final String goal) {
		this.goals.remove(goal);
	}

	public void addBelief(final String belief) {
		this.beliefs.add(belief);
	}

	public void removeBelief(final String belief) {
		this.beliefs.remove(belief);
	}

	public void addPercept(final String percept) {
		this.percepts.add(percept);
	}

	public void removePercept(final String percept) {
		this.percepts.remove(percept);
	}

	public void addMail(final String mail) {
		this.mails.add(mail);
	}

	public void removeMail(final String mail) {
		this.mails.remove(mail);
	}

	public void setFocus(final String focus) {
		if (this.focus.isEmpty() || !focus.equals(this.focus.peek())) {
			this.focus.push(focus);
		}
	}

	public void removeFocus() {
		if (!this.focus.isEmpty()) {
			final String focus = this.focus.pop();
			final String[] goals = this.goals.toArray(new String[this.goals.size()]);
			for (final String goal : goals) {
				if (goal.endsWith("[" + focus + "]")) {
					this.goals.remove(goal);
				}
			}
		}
	}

	public void setModule(final String module) {
		if (this.modules.isEmpty() || !module.equals(this.modules.peek())) {
			this.modules.push(module);
		}
	}

	public void removeModule() {
		if (!this.modules.isEmpty()) {
			this.modules.pop();
		}
	}

	public void setCondition(final Set<String> condition) {
		this.condition = condition;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public String getRunMode() {
		return this.runmode;
	}

	public Set<String> getGoals() {
		return this.goals;
	}

	public Set<String> getBeliefs() {
		return this.beliefs;
	}

	public Set<String> getPercepts() {
		return this.percepts;
	}

	public Set<String> getMails() {
		return this.mails;
	}

	public String getFocus() {
		if (this.focus.isEmpty()) {
			return "main";
		} else {
			return this.focus.peek();
		}
	}

	public String getModule() {
		if (this.modules.isEmpty()) {
			return "";
		} else {
			return this.modules.peek();
		}
	}

	public Set<String> getCondition() {
		return this.condition;
	}
}
