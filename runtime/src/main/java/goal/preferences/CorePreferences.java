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
package goal.preferences;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class CorePreferences {
	public enum Pref {
		/**
		 * print entities when they (dis)appear
		 */
		printEntities,
		/**
		 * full path to file containing learning results. Only used if learning is on.
		 */
		learnedBehaviourFile,
		/**
		 * true if learning is on, else false.
		 */
		learning,
		/**
		 * when user browses for an agent file, start browsing here
		 */
		agentsBrowseDir,
		/**
		 * true if we should remember which directory on file system user used last for
		 * loading or saving agents
		 */
		rememberLastUsedAgentDir,
		/**
		 * sleep agents when they receive same percepts and do same actions all the time
		 */
		sleepRepetitiveAgent,
		/**
		 * remove agent from the platform when it is killed
		 */
		removeKilledAgent,
		/**
		 * new agents copy environment run state (or run if no environment)
		 */
		agentCopyEnvState,
		/** old RunPreferences */
		breakOnGoalAchieved, globalBreakpoints, abortOnTestFailure, sequentialExecution;
	}

	private static Map<String, Object> preferences;

	public static void initPrefs(Map<String, Object> init) {
		if (init == null) {
			preferences = new TreeMap<>();
		} else {
			preferences = init;
		}
		init(Pref.printEntities, true);
		init(Pref.learnedBehaviourFile, "");
		init(Pref.learning, false);
		init(Pref.rememberLastUsedAgentDir, true);
		init(Pref.removeKilledAgent, false);
		init(Pref.agentCopyEnvState, true);
		init(Pref.sleepRepetitiveAgent, true);
		init(Pref.agentsBrowseDir, System.getProperty("user.dir") + File.separator + "GOALagents");
		init(Pref.breakOnGoalAchieved, true);
		init(Pref.globalBreakpoints, true);
		init(Pref.abortOnTestFailure, false);
		init(Pref.sequentialExecution, false);
	}

	public static Map<String, Object> getPrefs() {
		return Collections.unmodifiableMap(preferences);
	}

	/**
	 * check if new entities should be printed
	 *
	 * @return true if new entities should be printed. default true
	 */
	public static boolean getPrintEntities() {
		return (Boolean) get(Pref.printEntities);
	}

	/**
	 * Get the learner file, or empty string if no such file has been set. Note that
	 * you must also check #isLearning()
	 *
	 * @return String learner file.
	 */
	public static String getLearnFile() {
		return (String) get(Pref.learnedBehaviourFile);
	}

	public static boolean isLearning() {
		return (Boolean) get(Pref.learning);
	}

	/**
	 * check if last used directory should be remembered. Defaults to true.
	 *
	 * @return true if the last used directory should be remembered.
	 */
	public static boolean getRememberLastUsedAgentDir() {
		return (Boolean) get(Pref.rememberLastUsedAgentDir);
	}

	/**
	 * check if killed agents should be removed from the platform
	 *
	 * @return true if killed agents should be removed entirely (the default).
	 */
	public static boolean getRemoveKilledAgent() {
		return (Boolean) get(Pref.removeKilledAgent);
	}

	/**
	 * Check if the agent should copy its run state from the environment. So if env
	 * is running, the agent should go to running. For other environment states,
	 * agents goes to pause mode. If there is no environment, the agent should be
	 * set to running. Default value is false.
	 *
	 * @return true if agents should run automatically, else false.
	 */
	public static boolean getAgentCopyEnvRunState() {
		return (Boolean) get(Pref.agentCopyEnvState);
	}

	/**
	 * check if agents that repeat actions should be put to sleep till their percept
	 * input changes
	 *
	 * @return true if such agents should sleep, false else. Default is false.
	 */
	public static boolean getSleepRepeatingAgent() {
		return (Boolean) get(Pref.sleepRepetitiveAgent);
	}

	/**
	 * Get the path used when the user wants to browse for agents. Default is user's
	 * home dir as returned by {@link System#getProperty("user.home")}. This path
	 * may be set by the installer when the examples are installed in a user
	 * selected location. <br>
	 * Note that this path is not relevant for opening agents or MAS file, since
	 * these are stored by ABSOLUTE PATH anyway.
	 *
	 * @return browse path for agents.
	 */
	public static String getAgentBrowsePath() {
		return (String) get(Pref.agentsBrowseDir);
	}

	public static boolean getBreakOnGoalAchieved() {
		return (Boolean) get(Pref.breakOnGoalAchieved);
	}

	public static boolean getGlobalBreakpoints() {
		return (Boolean) get(Pref.globalBreakpoints);
	}

	public static boolean getAbortOnTestFailure() {
		return (Boolean) get(Pref.abortOnTestFailure);
	}

	public static boolean getSequentialExecution() {
		return (Boolean) get(Pref.sequentialExecution);
	}

	/**
	 * if new or removed entities should be printed
	 */
	public static void setPrintEntities(boolean printEntities) {
		put(Pref.printEntities, printEntities);
	}

	/**
	 * Adjust the learner file.
	 *
	 * @param learnerfile
	 *            is the learned-behaviour file to use, or empty string to disable.
	 */
	public static void setLearnFile(String learnerfile) {
		put(Pref.learnedBehaviourFile, learnerfile);
	}

	public static void setLearning(boolean learning) {
		put(Pref.learning, learning);
	}

	/**
	 * if last used directory should be remembered
	 */
	public static void setRememberLastUsedAgentDir(boolean rememberLastUsedAgentDir) {
		put(Pref.rememberLastUsedAgentDir, rememberLastUsedAgentDir);
	}

	/**
	 * if killed agents should be removed from the platform
	 */
	public static void setRemoveKilledAgent(boolean removeKilledAgent) {
		put(Pref.removeKilledAgent, removeKilledAgent);
	}

	/**
	 * If the agent should copy its run state from the environment. So if env is
	 * running, the agent should go to running. For other environment states, agents
	 * goes to pause mode. If there is no environment, the agent should be set to
	 * running.
	 */
	public static void setAgentCopyEnvRunState(boolean agentCopyEnvRunState) {
		put(Pref.agentCopyEnvState, agentCopyEnvRunState);
	}

	/**
	 * if agents that repeat actions should be put to sleep till their percept input
	 * changes
	 */
	public static void setSleepRepeatingAgent(boolean sleepRepetitiveAgent) {
		put(Pref.sleepRepetitiveAgent, sleepRepetitiveAgent);
	}

	/**
	 * Set the path used when the user wants to browse for agents. This path may be
	 * set by the installer when the examples are installed in a user selected
	 * location. <br>
	 * Note that this path is not relevant for opening agents or MAS file, since
	 * these are stored by ABSOLUTE PATH anyway.
	 */
	public static void setAgentBrowsePath(String agentsBrowseDir) {
		put(Pref.agentsBrowseDir, agentsBrowseDir);
	}

	public static void setGlobalBreakpoints(boolean globalBreakpoints) {
		put(Pref.globalBreakpoints, globalBreakpoints);
	}

	public static void setBreakOnGoalAchieved(boolean breakOnGoalAchieved) {
		put(Pref.breakOnGoalAchieved, breakOnGoalAchieved);
	}

	public static void setAbortOnTestFailure(boolean abortOnTestFailure) {
		put(Pref.abortOnTestFailure, abortOnTestFailure);
	}

	public static void setSequentialExecution(boolean sequentialExecution) {
		put(Pref.sequentialExecution, sequentialExecution);
	}

	// 3 helper functions...
	private static Object get(Pref pref) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		return preferences.get(pref.name());
	}

	private static void put(Pref pref, Object value) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		preferences.put(pref.name(), value);
	}

	private static void init(Pref pref, Object defaultValue) {
		Object current = get(pref);
		if (current == null || !current.getClass().equals(defaultValue.getClass())) {
			put(pref, defaultValue);
		}
	}

	/**
	 * Hide constructor.
	 */
	private CorePreferences() {
	}
}
