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
package goal.tools.adapt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import goal.core.executors.stack.ActionComboStackExecutor;
import goal.preferences.CorePreferences;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALDatabaseException;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.logging.InfoLog;
import languageTools.dependency.ModuleGraphGenerator;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.LaunchInstruction;
import mentalState.MentalState;
import mentalState.MentalStateWithEvents;
import mentalState.converter.GOALConversionUniverse;
import mentalState.converter.GOALMentalStateConverter;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * The generic learner which makes the link between the GOAL core and the
 * {@link LearnerAlgorithm}.
 * <h1>Learning</h1>
 * <p>
 * Learning is done through scripts, using the Batch runner to run an agent many
 * runs through the same problem, each time updating the score when the learning
 * is finished (see {@link BatchRunner}). The score is taken by the
 * {@link LearnerAlgorithm} to update the learnparameters. After the runs, the
 * entire Learner is saved to a file.
 * </p>
 * <h1>running after learning</h1>
 * <p>
 * After running, you can run GOAL to use a previously learned model. The
 * learner is then loaded from disk and hooked into GOAL, after which GOAL can
 * ask the learner to recommend actions.
 * </p>
 *
 */
public class FileLearner implements Serializable, Learner {

	/**
	 * Do not call this. Only for mocking this. This gives you a not properly
	 * initialized object. Reason for this: constructors should not call overridable
	 * methods; but we must initialize the field converter.
	 */
	protected FileLearner() {
	}

	/**
	 * The constructor static factory method.
	 *
	 * @param launch
	 * @param program
	 *
	 * @return new FileLearner.
	 */
	public static FileLearner createFileLearner(LaunchInstruction launch, AgentDefinition program) {
		return new FileLearner().init(launch, program);
	}

	/**
	 * Initializes a new learner. Override should call this to initialize this
	 * learner. Only for mocking this.
	 *
	 * @param launch
	 * @param program the {@link AgentDefinition} agent program.
	 */
	protected FileLearner init(LaunchInstruction launch, AgentDefinition program) {
		this.converter = getMentalStateConverter();
		this.launch = launch;
		this.lrnPrefix = launch.getAgentName();
		this.program = program;

		String filename = null;
		boolean loaded = false;
		/*
		 * If a file with a learned model has been specified then load this file.
		 */
		if (new File(filename = CorePreferences.getLearnFile()).exists()) {
			loaded = loadLearner(filename, program);
		}
		/*
		 * else if a agentname.lrn file exists in the current directory then load this
		 * file.
		 */
		else if (new File(filename = launch.getAgentName() + ".lrn").exists()) {
			loaded = loadLearner(filename, program);
		}

		if (!loaded) {
			this.runCount = 0;
		}

		/*
		 * Now for each adaptive module in the program initialize a new learning
		 * instance and start a new learning episode
		 */
		// FIXME: adaptive module should get its own learner...
		for (Module module : program.getAllReferencedModules()) {
			if (module.isAdaptive()) {
				ModuleID id = new ModuleID(module.getSignature());
				init(module, getAlgorithm(id));
				startEpisode(id);
			}
		}

		return this;
	}

	/**
	 * see {@link Learner#act(String, MentalStateWithEvents, List)}. The provided
	 * {@link #getMentalStateConverter()} is used to convert the mental states to
	 * strings. The private tables are used to convert these into integer numbers.
	 */
	@Override
	public ActionComboStackExecutor act(ModuleID module, MentalStateWithEvents ms,
			List<ActionComboStackExecutor> actionOptions) {
		this.updateCalled = false;
		ActionComboStackExecutor chosen = null;
		// Stores the list of input action options */
		Vector<ActionComboStackExecutor> options = new Vector<>();
		// Stores the action IDs associated with each ActionCombo
		Vector<Integer> optionids = new Vector<>();
		Hashtable<String, Boolean> added = new Hashtable<>();
		for (ActionComboStackExecutor option : actionOptions) {
			if (!added.containsKey(option.toString())) {
				added.put(option.toString(), true);
				options.add(option);
				// Observe and save the new option if we haven't seen it before
				String actionstr = option.getAction().applySubst(option.getParameters()).toString();
				processOption(actionstr);
				optionids.add(this.actionid.get(actionstr));
			}
		}

		// Observe and save the new state if we haven't seen it before
		String newstate = "";
		try {
			newstate = processState(ms, getBeliefFilter(module), getGoalFilter(module));
		} catch (GOALDatabaseException e) {
			// FIXME: Can't re-throw here;
			e.printStackTrace();
		}

		// Ask the module specific learner to pick the next action
		Integer newaction = this.learners.get(module).instance.nextAction(this.stateid.get(newstate),
				optionids.toArray(new Integer[0]));

		// Get the ActionCombo mapped to this action id
		chosen = options.elementAt(optionids.indexOf(newaction));

		// Increment the number of actions taken so far, for reporting
		this.learners.get(module).totalactions++;

		return chosen;
	}

	@Override
	public void update(ModuleID module, MentalStateWithEvents ms, double reward) {
		this.updateCalled = true;
		// Observe and save the new state if we haven't seen it before
		String newstate = "";
		try {
			newstate = processState(ms, getBeliefFilter(module), getGoalFilter(module));
		} catch (GOALDatabaseException e) {
			// FIXME: Cannot re-throw here
			e.printStackTrace();
		}
		// Call update on the module specific instance
		this.learners.get(module).instance.update(reward, this.stateid.get(newstate));
		// Accumulate the reward
		this.learners.get(module).totalreward += reward;
	}

	@Override
	public void terminate(MentalStateWithEvents ms, Double envReward) throws GOALRunFailedException {
		boolean writeLearnerToFile = false;
		/*
		 * Learning episodes are always terminated here. We do this once for all
		 * ADAPTIVE modules going from RUNNING->KILLED.
		 */
		for (Module module : this.program.getAllReferencedModules()) {
			ModuleID moduleId = new ModuleID(module.getSignature());
			if (module.isAdaptive()) {
				/*
				 * Learning was performed in this program so we will save the learner before we
				 * finish.
				 */
				writeLearnerToFile = true;

				// Increment the runCount;
				this.runCount++;

				if (!this.finishedEpisode || !this.updateCalled) {
					/*
					 * Obtain the reward from the environment. Or, if the environment does not
					 * support rewards, then create an internal reward based on whether the agent
					 * has achieved all its goals (reward +1) or not (it died instead, reward -1).
					 */
					boolean goalsEmpty;
					try {
						goalsEmpty = !ms.hasGoals();
					} catch (MSTDatabaseException | MSTQueryException e) {
						throw new GOALRunFailedException("failed to access the mental state.", e);
					}
					double reward = (envReward != null) ? envReward : goalsEmpty ? 1.0 : -1.0;
					if (!this.updateCalled) {
						update(moduleId, ms, reward);
					}
					if (!this.finishedEpisode) {
						finishEpisode(moduleId, ms, reward);
					}

				}
				/*
				 * Save the learning performance report for this episode to file
				 */
				writeReportFile(ms.getOwner().getName(), moduleId);
			}
		}

		if (writeLearnerToFile) {
			String filename = null;
			/*
			 * If a file with a learned model has been specified then save to this file.
			 */
			if (new File(filename = CorePreferences.getLearnFile()).exists()) {
				saveLearner(filename);
			}
			/*
			 * else save to agentname.lrn
			 */
			else {
				saveLearner(this.lrnPrefix + ".lrn");
			}
		}

	}

	/**
	 * Factory function
	 *
	 * @return the MentalStateConverter to use.
	 */
	public GOALMentalStateConverter getMentalStateConverter() {
		return new GOALMentalStateConverter();
	}

	/**
	 * Factory function
	 *
	 * @return Get the modulegraph generator.
	 */
	public ModuleGraphGenerator getGraphGenerator() {
		return new ModuleGraphGenerator();
	}

	/**
	 * Factory function.
	 *
	 * @param modulename the name of the module. Default impl will try to read file
	 *                   modulename + ".adaptive.properties" in current directory.
	 * @return {@link LearnerAlgorithm} Get a learner for given module
	 */
	public LearnerAlgorithm getLearner(ModuleID modulename) {
		/**
		 * FIXME: This function should be moved to the LearnerInterface and should be
		 * handled by the particular implementation.
		 */
		double sarsa_alpha = this.launch.getAlpha();
		double sarsa_gamma = this.launch.getGamma();
		double sarsa_epsilon = this.launch.getEpsilon();
		double sarsa_epsilon_decay = this.launch.getDecay();

		/* Use these defaults if we cannot load the properties file */
		Properties defaults = new Properties();
		defaults.setProperty("sarsa_alpha", Double.toString(sarsa_alpha));
		defaults.setProperty("sarsa_gamma", Double.toString(sarsa_gamma));
		defaults.setProperty("sarsa_epsilon", Double.toString(sarsa_epsilon));
		defaults.setProperty("sarsa_epsilon_decay", Double.toString(sarsa_epsilon_decay));
		Properties properties = new Properties(defaults);
		File file = new File(modulename + ".adaptive.properties");
		if (file.exists()) {
			try (FileInputStream fis = new FileInputStream(file.getName())) {
				properties.load(fis);
				new InfoLog("learned loaded properties from '" + file.getName() + "'.");
				new InfoLog(properties.toString());
			} catch (IOException e) {
				new Warning(
						"could not load learner properties from '" + file.getName() + "'; will proceed with defaults.",
						e);
			}
		}
		try {
			sarsa_alpha = Double.parseDouble(properties.getProperty("sarsa_alpha"));
			sarsa_epsilon = Double.parseDouble(properties.getProperty("sarsa_epsilon"));
			sarsa_epsilon_decay = Double.parseDouble(properties.getProperty("sarsa_epsilon_decay"));
			sarsa_gamma = Double.parseDouble(properties.getProperty("sarsa_gamma"));
		} catch (NumberFormatException e) {
			new Warning("failed to parse learner properties.", e);
		}

		return new QLearner(sarsa_alpha, sarsa_epsilon, sarsa_epsilon_decay, sarsa_gamma);
	}

	/********************************
	 * The private fields
	 *************************/
	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 4158712238978167789L;
	/**
	 * Provides each adaptive module with its own learner.
	 */
	private Map<ModuleID, LearnerInstance> learners = new HashMap<>();
	/**
	 * Provides each adaptive module with its own filter.
	 */
	private final Map<ModuleID, Set<String>> belieffilters = new HashMap<>();
	private final Map<ModuleID, Set<String>> goalfilters = new HashMap<>();
	/**
	 * The map <action IDs, value>. actions are stored as strings using
	 * {@link ActionCombo#toString()}.
	 */
	private Map<String, Integer> actionid = new TreeMap<>();
	/**
	 * The list of GOAL state IDs. GOALState is a number representing a
	 * {@link MentalState}. See also
	 * {@link GOALMentalStateConverter#translate(MentalState, java.util.Stack)}.
	 */
	private Map<String, Integer> stateid = new TreeMap<>();
	private Map<Integer, String> actionstr = new TreeMap<>();
	private Map<String, String> statestr = new TreeMap<>();
	private GOALMentalStateConverter converter;
	/** Used to save the converter universe */
	private List<String> universe;
	/** The program that this learner is associated with */
	private LaunchInstruction launch;
	private AgentDefinition program;
	/** File name prefix for .lrn file */
	private String lrnPrefix;
	private Integer runCount;
	private boolean finishedEpisode;
	private boolean updateCalled;

	/********************* SUPPORT FUNCTIONS ***********************/
	/**
	 * Initialize a new learning instance for the given adaptive module. Looks for a
	 * default property file in the working directory. The name of the file should
	 * be {module name}.adaptive.properties.
	 *
	 * @param module
	 */
	private void init(Module module, LearnerAlgorithm learner) {
		ModuleID moduleID = new ModuleID(module.getSignature());
		// Associate filters with corresponding rule set.
		setBeliefFilter(module);
		setGoalFilter(module);

		// Create a new Q-learner.
		if (learner == null) {
			learner = getLearner(moduleID);
		}
		// Associate learner with module.
		setAlgorithm(moduleID, learner);
	}

	/**
	 * Compute belief filter for rule set of the module.
	 *
	 * @param module
	 */
	private void setBeliefFilter(Module module) {
		this.belieffilters.put(new ModuleID(module.getSignature()), module.getLearnedBeliefs());
	}

	private Set<String> getBeliefFilter(ModuleID module) {
		return this.belieffilters.get(module);
	}

	/**
	 * Compute goal filter for rule set of the module.
	 *
	 * @param module
	 */
	private void setGoalFilter(Module module) {
		this.goalfilters.put(new ModuleID(module.getSignature()), module.getLearnedGoals());
	}

	private Set<String> getGoalFilter(ModuleID module) {
		return this.goalfilters.get(module);
	}

	/**
	 * Starts a new learning episode for the given module.
	 *
	 * @param module
	 */
	private void startEpisode(ModuleID module) {
		this.learners.get(module).instance.start();
		this.learners.get(module).totalreward = 0;
		this.learners.get(module).totalactions = 0;
		this.finishedEpisode = false;
	}

	/**
	 * Sets an algorithm for the learner. You can change the learning algorithm
	 * without loosing the {@link GOALConversionUniverse} of known states. However,
	 * the things learned by the {@link LearnerAlgorithm} will get lost. So you
	 * usually do not want to change this after learning.
	 *
	 * @param algorithm is an instance of {@link LearnerAlgorithm}
	 */
	private void setAlgorithm(ModuleID module, LearnerAlgorithm algorithm) {
		this.learners.put(module, new LearnerInstance(algorithm));
	}

	private LearnerAlgorithm getAlgorithm(ModuleID module) {
		return this.learners.containsKey(module) ? this.learners.get(module).instance : null;
	}

	/**
	 * Finish the current learning episode for the given adaptive module.
	 *
	 * @param module
	 */
	private void finishEpisode(ModuleID module, MentalStateWithEvents ms, double reward) {
		this.finishedEpisode = true;
		// Observe and save the new state if we haven't seen it before
		try {
			processState(ms, getBeliefFilter(module), getGoalFilter(module));
		} catch (GOALDatabaseException e) {
			// FIXME: Cannot re-throw here
			e.printStackTrace();
		}
		// Call finish on the module specific instance
		this.learners.get(module).instance.finish(reward);
		// Accumulate the reward
		this.learners.get(module).totalreward += reward;
	}

	/**
	 * Writes the learning reports for the given module to a file in the working
	 * directory. The name of the output file will be {module name}.adaptive.out.
	 *
	 * @param agentName
	 * @param module
	 */
	private void writeReportFile(String agentName, ModuleID module) {
		/* Write the performance results to file */
		String outfile = module.makeFileName() + ".adaptive.out";
		try (BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true))) {
			out.write(String.format("%s: %.2f %.2f %07d\n", agentName, this.learners.get(module).totalactions,
					this.learners.get(module).totalreward, this.stateid.size()));
		} catch (IOException e) {
			new Warning("could not write report '" + outfile + "', but continuing.", e);
		}
		/* Write human readable learning output to file */
		outfile = module.makeFileName() + ".lrn.txt";
		try (BufferedWriter out = new BufferedWriter(new FileWriter(outfile, false))) {
			String summary = "";
			summary += "-----------------------------------------\n";
			summary += String.format("%-30s: %d\n", "Number of runs", this.runCount);
			summary += String.format("%-30s: %d\n", "Situations encountered (below)", this.stateid.size());
			summary += "-----------------------------------------\n";
			out.write(summary);
			int index = 0;
			for (String state : this.stateid.keySet()) {
				out.write(String.format("\ns%07d %s", index, this.statestr.get(state)));
				Hashtable<Integer, Double> avpairs = this.learners.get(module).instance
						.actionValues(this.stateid.get(state));
				List<Integer> sortedByValue = new ArrayList<>(avpairs.keySet().size());
				// FIXME this is a custom sorting from high to low value.
				for (Integer i : avpairs.keySet()) {
					boolean added = false;
					for (int j = 0; j < sortedByValue.size(); j++) {
						if (avpairs.get(i) >= avpairs.get(sortedByValue.get(j))) {
							sortedByValue.add(j, i);
							added = true;
							break;
						}
					}
					if (!added) {
						sortedByValue.add(i);
					}
				}
				String s = "";
				for (Integer i : sortedByValue) {
					s += String.format("%20s : %+06.3f\n", this.actionstr.get(i), avpairs.get(i));
				}
				out.write(s);
				index++;
			}
		} catch (IOException e) {
			new Warning("could not write report '" + outfile + "'.", e);
		}
	}

	/**
	 * Returns the MentalState translated to a state vector string. The filter is
	 * applied to the MentalState before it is translated. The returned state is
	 * also added to the list of known states with a unique ID, if it is not already
	 * there.
	 *
	 * @param ms
	 * @param belieffilter
	 * @param goalfilter
	 * @return
	 * @throws GOALDatabaseException
	 */
	private String processState(MentalStateWithEvents ms, Set<String> belieffilter, Set<String> goalfilter)
			throws GOALDatabaseException {
		try {
			return this.converter.getStateString(ms, this.stateid, this.statestr, belieffilter, goalfilter);
		} catch (MSTDatabaseException | MSTQueryException e) {
			throw new GOALDatabaseException("could not process the mental state.", e);
		}
	}

	/**
	 * Adds the option to the list of known options if not already there
	 *
	 * @param action the action/option that was taken.
	 */
	private void processOption(String action) {

		if (!this.actionid.containsKey(action)) {
			this.actionid.put(action, this.actionid.size() + 1);
		}
		if (!this.actionstr.containsKey(this.actionid.get(action))) {
			this.actionstr.put(this.actionid.get(action), action);
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// oos.defaultWriteObject();
		oos.writeObject(this.runCount);
		oos.writeObject(this.learners);
		oos.writeObject(this.actionid);
		oos.writeObject(this.stateid);
		oos.writeObject(this.actionstr);
		oos.writeObject(this.statestr);
		this.universe = this.converter.getUniverse().toStringArray();
		oos.writeObject(this.universe);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// is.defaultReadObject();
		this.runCount = (Integer) ois.readObject();
		this.learners = (Map<ModuleID, LearnerInstance>) ois.readObject();
		this.actionid = (Map<String, Integer>) ois.readObject();
		this.stateid = (Map<String, Integer>) ois.readObject();
		this.actionstr = (Map<Integer, String>) ois.readObject();
		this.statestr = (Map<String, String>) ois.readObject();
		this.universe = (List<String>) ois.readObject();
		this.converter = getMentalStateConverter();
		this.converter.getUniverse().setPreassignedIndices(this.universe);
	}

	/**
	 * Saves the learning to file
	 *
	 * @param file
	 */
	private void saveLearner(String file) {
		try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			output.writeObject(this);
		} catch (IOException e) {
			new Warning("'" + file + "' could not be written, but continuing.", e);
		}
	}

	/**
	 * Loads the learning from file
	 *
	 * @param file
	 * @param program
	 * @return a {@link FileLearner} object
	 */
	private boolean loadLearner(String file, AgentDefinition program) {
		try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			Object obj = input.readObject();
			FileLearner l = (FileLearner) obj;
			this.runCount = l.runCount;
			this.learners = l.learners;
			this.actionid = l.actionid;
			this.stateid = l.stateid;
			this.actionstr = l.actionstr;
			this.statestr = l.statestr;
			this.universe = l.universe;
			this.converter = l.converter;
			new InfoLog("loading learned model from '" + file + "'.");
			return true;
		} catch (IOException | ClassNotFoundException e) {
			new Warning("learner file '" + file + "' could not be read, but continuing anyway.", e);
		}
		return false;
	}

}

/**
 * Holds an instance of a LearningAlgorithm along with records of use
 */
class LearnerInstance implements Serializable {
	private static final long serialVersionUID = -8539363627078273749L;
	protected LearnerAlgorithm instance;
	/**
	 * Accumulates the total reward received from start to finish.
	 */
	protected double totalreward = 0;
	/**
	 * Counts the total number of actions performed from start to finish.
	 */
	protected double totalactions = 0;

	protected LearnerInstance(LearnerAlgorithm instance) {
		this.instance = instance;
		this.totalreward = 0;
		this.totalactions = 0;
	}
}
